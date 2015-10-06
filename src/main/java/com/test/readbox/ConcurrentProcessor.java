package com.test.readbox;

import java.util.Collection;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.test.readbox.internal.ConcurrentReader;
import com.test.readbox.internal.data.DataInternal;
import com.test.readbox.internal.data.FileInfo;

@Singleton
public class ConcurrentProcessor {
	private static final String ROOT_FOLDER_ID = "0";

	private final ConcurrentReader reader;

	@Inject
	public ConcurrentProcessor(ConcurrentReader reader) {
		this.reader = reader;
	}

	public Collection<FileInfo> getResults(BoxCredentials credentials) throws InterruptedException {
		DataInternal data = new DataInternal(credentials);
		data.getThreadsCounter().inc();
		reader.read(ROOT_FOLDER_ID, data);
		Thread.yield();
		data.getThreadsCounter().await();
		return data.getResultsAccumulator().getResults();
	}
}
