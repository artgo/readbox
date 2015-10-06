package com.test.readbox.internal;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.test.readbox.box.BoxPageFetcher;
import com.test.readbox.box.BoxPageFetcherBoxSdkImpl;

public class BoxModule extends AbstractModule {
	@Override
	protected void configure() {
		// bind(Executor.class).toInstance(Runnable::run);
		bind(Executor.class).toInstance(Executors.newFixedThreadPool(8));
		bind(BoxPageFetcher.class).to(BoxPageFetcherBoxSdkImpl.class).asEagerSingleton();
		//bind(BoxPageFetcher.class).to(BoxPageFetcherHttpClientImpl.class).asEagerSingleton();
	}

	public void shutdown(Injector injector) {
		BoxPageFetcher fetcher = injector.getInstance(BoxPageFetcher.class);
		fetcher.shutdown();

		Executor executor = injector.getInstance(Executor.class);
		if (executor instanceof ExecutorService) {
			ExecutorService execService = (ExecutorService) executor;
			execService.shutdown();
		}
	}
}
