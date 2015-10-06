package com.test.readbox.internal;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.test.readbox.BoxCredentials;
import com.test.readbox.box.BoxPage;
import com.test.readbox.box.BoxPageFetcher;
import com.test.readbox.internal.data.FoldersAndFiles;

@Singleton
public class ConcurrentContentFetcher {
	private static final Logger log = LogManager.getLogger(ConcurrentContentFetcher.class);

	private static final String ID_FIELD = "id";
	private static final String NAME_FIELD = "name";
	private static final String MODIFIED_AT_FIELD = "content_modified_at";
	private static final int BATCH_SIZE = 1000; // This is the maximum possible batch size.
	private static final int CONCURRENT_THRESHHOLD = 5; // Do not use threads if there not enough data.

	private final BoxPageFetcher boxPageFetcher;
	private final Executor executor;

	@Inject
	public ConcurrentContentFetcher(BoxPageFetcher boxPageFetcher, Executor executor) {
		this.boxPageFetcher = boxPageFetcher;
		this.executor = executor;
	}

	public FoldersAndFiles fetch(BoxCredentials credentials, String folderId) throws InterruptedException {
		FoldersAndFiles foldersAndFiles = new FoldersAndFiles();

		BoxPage page = boxPageFetcher.getPage(credentials, folderId, 0, BATCH_SIZE, ID_FIELD, NAME_FIELD, MODIFIED_AT_FIELD);

		CountDownLatch latch = null;

		// If there is data for at least one more batch
		long total = page.getTotal();
		if (total > BATCH_SIZE) {
			int batches = (int) (total/BATCH_SIZE);
			if ((total % BATCH_SIZE) > 0) {
				batches++;
			}
			log.debug("Processing {} batches", batches);

			if (batches > CONCURRENT_THRESHHOLD) {
				log.debug("Load data concurrently");
				// We will process one batch in current thread, so only if there is > 1 we will kick them off in parallel
				if (batches > 2) {
					CountDownLatch newLatch = new CountDownLatch(batches - 2);
					// Start from 2, since we already loaded batch 0 and we process batch N 1 in this thread;
					for (int i = 2; i < batches; i++) {
						int batchN = i;
						executor.execute(() -> loadAndProcess(credentials, newLatch, folderId, foldersAndFiles, batchN));
					}
					latch = newLatch;
				}

				loadAndProcess(credentials, null, folderId, foldersAndFiles, 1);
			} else {
				log.debug("Load data sequentially");
				// Start from 1 since batch N0 has already been read
				for (int i = 1; i < batches; i++) {
					loadAndProcess(credentials, null, folderId, foldersAndFiles, i);
				}
			}
		}

 		foldersAndFiles.addPage(page);

 		if (latch != null) {
 			latch.await();
 		}

		return foldersAndFiles;
	}

	private void loadAndProcess(BoxCredentials credentials, CountDownLatch latch, String folderId, FoldersAndFiles foldersAndFiles, int batchN) {
		try {
			BoxPage page = boxPageFetcher.getPage(credentials, folderId, batchN * BATCH_SIZE, BATCH_SIZE, ID_FIELD, NAME_FIELD, MODIFIED_AT_FIELD);
			foldersAndFiles.addPage(page);
		} finally {
			if (latch != null) {
				latch.countDown();
			}
		}
	}
}
