package com.test.readbox.box;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.box.sdk.PartialCollection;
import com.test.readbox.BoxCredentials;
import com.test.readbox.internal.data.FileInfo;

public class BoxPageFetcherBoxSdkImpl implements BoxPageFetcher {
	private static final Logger log = LogManager.getLogger(BoxPageFetcherBoxSdkImpl.class);

	public BoxPageFetcherBoxSdkImpl() {
		System.setProperty("http.keepAlive", "true");
	}

	@Override
	public BoxPage getPage(BoxCredentials credentials, String folderId, long offset, int limit, String... fields) {
		BoxFolder remoteFolder = new BoxFolder(credentials.getBoxApiConnection(), folderId);
		PartialCollection<BoxItem.Info> items = remoteFolder.getChildrenRange(offset, limit, fields);

		BoxPage page = new BoxPage(items.fullSize());
		for (BoxItem.Info itemInfo : items) {
	    	FileInfo fi = new FileInfo(itemInfo.getID(), folderId, itemInfo.getName(), itemInfo.getContentModifiedAt());
		    if (itemInfo instanceof BoxFolder.Info) {
		    	page.getFolders().add(fi);
		    } else {
		    	page.getFiles().add(fi);
		    }
		    log.info("Loaded {}", itemInfo.getName());
		}

		return page;
	}

}
