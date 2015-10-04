package com.test.readbox;

import java.util.Collection;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.test.readbox.accumulator.ResultsAccumulator;

@Singleton
public class ConcurrentProcessor {
	private static final String ROOT_FOLDER_ID = "0";
	
	private final ResultsAccumulator resultsAccumulator;
	private final CounterService counterService;
	private final ConcurrentReader reader;
	
	@Inject
	public ConcurrentProcessor(ResultsAccumulator resultsAccumulator, CounterService counterService, ConcurrentReader reader) {
		this.resultsAccumulator = resultsAccumulator;
		this.counterService = counterService;
		this.reader = reader;
	}

	public Collection<FileInfo> getResults() throws InterruptedException {
		counterService.inc();
		reader.read(ROOT_FOLDER_ID);
		Thread.yield();
		counterService.await();
		return resultsAccumulator.getResults();
	}
}
