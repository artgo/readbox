package com.test.readbox;

import lombok.Getter;

import com.box.sdk.BoxAPIConnection;

@Getter
public class BoxCredentials {
	private final BoxAPIConnection boxApiConnection;

	public BoxCredentials(String clientId, String clientSecret, String accessToken, String refreshToken, long lastRefresh, long expiresIn) {
		boxApiConnection = new BoxAPIConnection(clientId, clientSecret, accessToken, refreshToken);
		boxApiConnection.setLastRefresh(lastRefresh);
		boxApiConnection.setExpires(expiresIn);
	}

	public String getAccessToken() {
		return boxApiConnection.getAccessToken();
	}

	public String getRefreshToken() {
		return boxApiConnection.getRefreshToken();
	}
}
