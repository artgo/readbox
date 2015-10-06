package com.test.readbox.internal.data;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class ThreadsCounter {
	private final AtomicLong counter = new AtomicLong(0L);
	private final CountDownLatch latch = new CountDownLatch(1);

	public void inc() {
		counter.incrementAndGet();
	}

	public void dec() {
		long value = counter.decrementAndGet();
		// We are fine to miss some cases where we countDown() more than once
		if (value == 0 && latch.getCount() > 0) {
			latch.countDown();
		}
	}

	public void await() throws InterruptedException {
		latch.await();
	}
}
