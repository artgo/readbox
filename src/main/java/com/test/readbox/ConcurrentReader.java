package com.test.readbox;

import java.util.concurrent.Executor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.test.readbox.accumulator.ResultsAccumulator;

@Singleton
public class ConcurrentReader {
	private static final Logger log = LogManager.getLogger(ConcurrentReader.class);

	private final ResultsAccumulator resultsAccumulator;
	private final BoxFetcher fetcher;
	private final CounterService counterService;
	private final Executor executor;

	@Inject
	public ConcurrentReader(ResultsAccumulator resultsAccumulator, BoxFetcher fetcher, Executor executor, CounterService counterService) {
		this.resultsAccumulator = resultsAccumulator;
		this.fetcher = fetcher;
		this.counterService = counterService;
		this.executor = executor;
	}

	public void read(String folderId) {
		try {
			try {
				FoldersAndFiles foldersAndFiles = fetcher.fetch(folderId);

				int lastN = foldersAndFiles.getFolders().size();
				int current = 1;
				for (FileInfo nestedFolder : foldersAndFiles.getFolders()) {
					counterService.inc();
					if (current++ < lastN) {
						executor.execute(() -> read(nestedFolder.getId()));
					} else {
						// Reuse same thread
						read(nestedFolder.getId());
					}
				}

				resultsAccumulator.addAll(foldersAndFiles.getFiles());
			} catch (InterruptedException e) {
				log.error("Failed to read data", e);
			}
		} finally {
			counterService.dec();
		}
	}
}
