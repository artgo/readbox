package com.test.readbox.internal.data;

import lombok.Getter;

import com.test.readbox.BoxCredentials;
import com.test.readbox.internal.accumulator.ResultsAccumulator;

@Getter
public class DataInternal {
	private final BoxCredentials boxCredentials;
	private final ResultsAccumulator resultsAccumulator;
	private final ThreadsCounter threadsCounter;

	public DataInternal(BoxCredentials credentials) {
		this.boxCredentials = credentials;
		this.resultsAccumulator = new ResultsAccumulator();
		this.threadsCounter = new ThreadsCounter();
	}
}
