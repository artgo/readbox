package com.test.readbox.internal;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.test.readbox.internal.data.DataInternal;
import com.test.readbox.internal.data.FileInfo;
import com.test.readbox.internal.data.FoldersAndFiles;

@Singleton
public class ConcurrentReader {
	private static final Logger log = LogManager.getLogger(ConcurrentReader.class);

	private final ConcurrentContentFetcher fetcher;
	private final Executor executor;

	@Inject
	public ConcurrentReader(ConcurrentContentFetcher fetcher, Executor executor) {
		this.fetcher = fetcher;
		this.executor = executor;
	}

	public void read(String folderId, DataInternal data) {
		try {
			try {
				FoldersAndFiles foldersAndFiles = fetcher.fetch(data.getBoxCredentials(), folderId);

				// Accumulate data first to increase chance to create directory filter
				data.getResultsAccumulator().addAll(foldersAndFiles.getFiles());

				Collection<FileInfo> folders = foldersAndFiles.getFolders().stream()
						.filter(f -> !data.getResultsAccumulator().hasDateLimit() || data.getResultsAccumulator().getDateLimit().before(f.getLastModified()))
						.collect(Collectors.toList());

				int lastN = folders.size();
				int current = 1;
				for (FileInfo nestedFolder : folders) {
				    //log.info("Processing folder {}", nestedFolder.getName());
					data.getThreadsCounter().inc();
					if (current++ < lastN) {
						executor.execute(() -> read(nestedFolder.getId(), data));
					} else {
						// Reuse same thread
						read(nestedFolder.getId(), data);
					}
				}
			} catch (InterruptedException e) {
				log.error("Failed to read data", e);
			}
		} finally {
			data.getThreadsCounter().dec();
		}
	}
}
