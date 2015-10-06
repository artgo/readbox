package com.test.readbox.box;

import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.protocol.HttpContext;

public class NginxKeepAliveStrategy extends DefaultConnectionKeepAliveStrategy {

	@Override
	public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
		long original = super.getKeepAliveDuration(response, context);
		if (original > 0) {
			return original;
		}
		return 75_000; // 75 Seconds (default Nginx timeout)
	}
}
