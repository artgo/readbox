package com.test.readbox;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.StringUtils;

import com.box.sdk.BoxAPIConnection;
import com.google.common.base.Stopwatch;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class App {
	private static final String BOX_REFRESH_TOKEN = "BOX_REFRESH_TOKEN";
	private static final String BOX_ACCESS_TOKEN = "BOX_ACCESS_TOKEN";
	private static final String BOX_CLIENT_SECRET = "BOX_CLIENT_SECRET";
	private static final String BOX_CLIENT_ID = "BOX_CLIENT_ID";

	public static void main(String[] args) throws InterruptedException {
		System.out.println("Starting...");

		Stopwatch stopwatch = Stopwatch.createStarted();

		String clientId = getEnv(BOX_CLIENT_ID);
		String clientSecret = getEnv(BOX_CLIENT_SECRET);
		String accessToken = getEnv(BOX_ACCESS_TOKEN);
		String refreshToken = getEnv(BOX_REFRESH_TOKEN);
		BoxAPIConnection api = new BoxAPIConnection(clientId, clientSecret, accessToken, refreshToken);
		api.setLastRefresh(System.currentTimeMillis());
		api.setExpires(3_600_000);

		Injector injector = Guice.createInjector(new BoxModule(api));
		ConcurrentProcessor processor = injector.getInstance(ConcurrentProcessor.class);

		Collection<FileInfo> results = processor.getResults();

		stopwatch.stop();

		System.out.println("Elapsed: " + stopwatch);
		results.stream().forEach(System.out::println);

		System.out.println("access: " + api.getAccessToken());
		System.out.println("refresh: " + api.getRefreshToken());

		Executor executor = injector.getInstance(Executor.class);
		if (executor instanceof ExecutorService) {
			ExecutorService execService = (ExecutorService) executor;
			execService.shutdown();
		}
	}

	private static String getEnv(String envName) {
		String result = System.getenv(envName);
		if (StringUtils.isBlank(result)) {
			System.out.println("All of the following environment variables must be setup:");
			System.out.println(BOX_CLIENT_ID);
			System.out.println(BOX_CLIENT_SECRET);
			System.out.println(BOX_ACCESS_TOKEN);
			System.out.println(BOX_REFRESH_TOKEN);
			System.out.println("Use http://box-token-generator.herokuapp.com to get those...");
			throw new IllegalArgumentException("One of environment variables is not setup");
		}
		return result;
	}
}
