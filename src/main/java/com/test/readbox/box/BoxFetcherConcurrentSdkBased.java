package com.test.readbox.box;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.box.sdk.PartialCollection;
import com.google.inject.Inject;
import com.test.readbox.BoxFetcher;
import com.test.readbox.FileInfo;
import com.test.readbox.FoldersAndFiles;
import com.test.readbox.accumulator.ResultsAccumulator;

public class BoxFetcherConcurrentSdkBased implements BoxFetcher {
	private static final Logger log = LogManager.getLogger(BoxFetcherConcurrentSdkBased.class);
	private static final String ID_FIELD = "id";
	private static final String NAME_FIELD = "name";
	private static final String MODIFIED_AT_FIELD = "modified_at";
	private static final int BATCH_SIZE = 1000; // This is the maximum possible batch size.
	private static final int CONCURRENT_THRESHHOLD = 5; // Do not use threads if there not enough data.

	private final BoxAPIConnection api;
	private final Executor executor;
	private final ResultsAccumulator resultsAccumulator;

	@Inject
	public BoxFetcherConcurrentSdkBased(BoxAPIConnection api, Executor executor, ResultsAccumulator resultsAccumulator) {
		System.setProperty("http.keepAlive", "true");
		this.api = api;
		this.executor = executor;
		this.resultsAccumulator = resultsAccumulator;
	}

	@Override
	public FoldersAndFiles fetch(String folderId) throws InterruptedException {
		FoldersAndFiles foldersAndFiles = new FoldersAndFiles();
		BoxFolder remoteFolder = new BoxFolder(api, folderId);

		PartialCollection<BoxItem.Info> items = remoteFolder.getChildrenRange(0, BATCH_SIZE, ID_FIELD, NAME_FIELD, MODIFIED_AT_FIELD);

		CountDownLatch latch = null;

		// If there is data for at least one more batch
		if (items.fullSize() > BATCH_SIZE) {
			int batches = (int) (items.fullSize()/BATCH_SIZE);
			if ((items.fullSize() % BATCH_SIZE) > 0) {
				batches++;
			}

			if (batches > CONCURRENT_THRESHHOLD) {
				// We will process one batch in current thread, so only if there is > 1 we will kick them off in parallel
				if (batches > 2) {
					CountDownLatch newLatch = new CountDownLatch(batches - 2);
					// Start from 2, since we already loaded batch 0 and we process batch N 1 in this thread;
					for (int i = 2; i < batches; i++) {
						int batchN = i;
						executor.execute(() -> loadAndProcess(remoteFolder, newLatch, folderId, foldersAndFiles, batchN));
					}
					latch = newLatch;
				}

				loadAndProcess(remoteFolder, null, folderId, foldersAndFiles, 1);
			} else {
				// Start from 1 since batch N0 has already been read
				for (int i = 1; i < batches; i++) {
					loadAndProcess(remoteFolder, null, folderId, foldersAndFiles, i);
				}
			}
		}

 		processData(folderId, foldersAndFiles, items);

 		if (latch != null) {
 			latch.await();
 		}

		return foldersAndFiles;
	}

	private void loadAndProcess(BoxFolder remoteFolder, CountDownLatch latch, String folderId, FoldersAndFiles foldersAndFiles, int batchN) {
		try {
			PartialCollection<BoxItem.Info> items = remoteFolder.getChildrenRange(batchN * BATCH_SIZE, BATCH_SIZE, ID_FIELD, NAME_FIELD, MODIFIED_AT_FIELD);
			processData(folderId, foldersAndFiles, items);
		} finally {
			if (latch != null) {
				latch.countDown();
			}
		}
	}

	private void processData(String folderId, FoldersAndFiles foldersAndFiles, PartialCollection<BoxItem.Info> items) {
		for (BoxItem.Info itemInfo : items) {
			if (!resultsAccumulator.hasDateLimit() || resultsAccumulator.getDateLimit().before(itemInfo.getModifiedAt())) {
		    	FileInfo fi = new FileInfo(itemInfo.getID(), folderId, itemInfo.getName(), itemInfo.getModifiedAt());
			    if (itemInfo instanceof BoxFolder.Info) {
			        foldersAndFiles.getFolders().add(fi);
			    } else {
			        foldersAndFiles.getFiles().add(fi);
			    }
			}
		    log.debug("Processed {}", itemInfo.getName());
		}
	}
}
