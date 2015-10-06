package com.test.readbox.box;

import static com.test.readbox.box.BoxConstants.CONTENT_MODIFIED_AT_FIELD;
import static com.test.readbox.box.BoxConstants.ID_FIELD;
import static com.test.readbox.box.BoxConstants.NAME_FIELD;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.base.Joiner;
import com.test.readbox.BoxCredentials;
import com.test.readbox.internal.data.FileInfo;

public class BoxPageFetcherHttpClientImpl implements BoxPageFetcher {
	private static final String FOLDER_TYPE = "folder";
	private static final Logger log = LogManager.getLogger(BoxPageFetcherHttpClientImpl.class);
	private static final String URL_PATTERN = "https://api.box.com/2.0/folders/%s/items?offset=%d&limit=%d&fields="
												+ Joiner.on(",").join(ID_FIELD, NAME_FIELD, CONTENT_MODIFIED_AT_FIELD);

	private final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
	private final NginxKeepAliveStrategy keepAliveStrategy = new NginxKeepAliveStrategy();
	private final CloseableHttpClient httpClient;
	private final ObjectMapper mapper = new ObjectMapper();
	private final ObjectReader objectReader;

	public BoxPageFetcherHttpClientImpl() {
		// Increase max total connection to 7
		connectionManager.setMaxTotal(7);
		// Increase default max connection per route to 7
		connectionManager.setDefaultMaxPerRoute(7);

		httpClient = HttpClients.custom()
				.setConnectionManager(connectionManager)
				.setKeepAliveStrategy(keepAliveStrategy) // Make sure keep alive
				.build();

		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectReader = mapper.readerFor(PageListResult.class);
	}

	@Override
	public BoxPage getPage(BoxCredentials credentials, String folderId, long offset, int limit) {
		HttpGet get = new HttpGet(String.format(URL_PATTERN, folderId, offset, limit));
		get.addHeader("Authorization", "Bearer " + credentials.getAccessToken());

		PageListResult result;
		try {
			CloseableHttpResponse response = httpClient.execute(get);
			// Process return code
			InputStream is = response.getEntity().getContent();
			result = objectReader.readValue(is);
		} catch (IOException e) {
			log.error("Error reading data", e);
			throw new RuntimeException(e);
			// TODO: retry/refresh token
		}

		if (result == null || result.getEntries() == null) {
			// TODO: backoff + retry
			return new BoxPage(0);
		}

		BoxPage page = new BoxPage(result.getTotal());
		for (ItemInfo item : result.getEntries()) {
			FileInfo fi = new FileInfo(item.getId(), folderId, item.getName(), item.getContentModifiedAt());
			if (FOLDER_TYPE.equals(item.getType())) {
				page.getFolders().add(fi);
			} else {
				page.getFiles().add(fi);
			}
			//log.info("Loaded {}", item.getName());
		}

		return page;
	}

	@Override
	public void shutdown() {
		try {
			httpClient.close();
		} catch (IOException e) {
			log.error("Error closing http client", e);
		}
		connectionManager.shutdown();
	}
}
