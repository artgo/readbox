package com.test.readbox;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Stopwatch;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.test.readbox.internal.BoxModule;
import com.test.readbox.internal.data.FileInfo;

public class App {
	private static final String BOX_REFRESH_TOKEN = "BOX_REFRESH_TOKEN";
	private static final String BOX_ACCESS_TOKEN = "BOX_ACCESS_TOKEN";
	private static final String BOX_CLIENT_SECRET = "BOX_CLIENT_SECRET";
	private static final String BOX_CLIENT_ID = "BOX_CLIENT_ID";
	static final long SIXTY_DAYS = 5184000000L;

	public static void main(String[] args) throws InterruptedException {
		System.out.println("Starting...");

		Stopwatch stopwatch = Stopwatch.createStarted();

		String clientId = getEnv(BOX_CLIENT_ID);
		String clientSecret = getEnv(BOX_CLIENT_SECRET);
		String accessToken = getEnv(BOX_ACCESS_TOKEN);
		String refreshToken = getEnv(BOX_REFRESH_TOKEN);
		BoxCredentials credentials = new BoxCredentials(clientId, clientSecret, accessToken, refreshToken, System.currentTimeMillis(), SIXTY_DAYS);

		BoxModule module = new BoxModule();
		Injector injector = Guice.createInjector(module);
		ConcurrentProcessor processor = injector.getInstance(ConcurrentProcessor.class);

		Collection<FileInfo> results = processor.getResults(credentials);

		stopwatch.stop();

		System.out.println("Elapsed: " + stopwatch);
		results.stream().forEach(System.out::println);

		System.out.println("access: " + credentials.getAccessToken());
		System.out.println("refresh: " + credentials.getRefreshToken());

		module.shutdown(injector);
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
