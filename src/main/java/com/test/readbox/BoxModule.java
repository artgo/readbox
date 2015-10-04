package com.test.readbox;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.box.sdk.BoxAPIConnection;
import com.google.inject.AbstractModule;
import com.test.readbox.box.BoxFetcherConcurrentSdkBased;

public class BoxModule extends AbstractModule {
	private final BoxAPIConnection api;

	public BoxModule(BoxAPIConnection api) {
		this.api = api;
	}

	@Override
	protected void configure() {
		//bind(Executor.class).toInstance(Runnable::run);
		bind(Executor.class).toInstance(Executors.newFixedThreadPool(8));
		bind(BoxAPIConnection.class).toInstance(api);
		bind(BoxFetcher.class).to(BoxFetcherConcurrentSdkBased.class).asEagerSingleton();		
	}
}
