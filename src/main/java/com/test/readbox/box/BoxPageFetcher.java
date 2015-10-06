package com.test.readbox.box;

import com.test.readbox.BoxCredentials;

public interface BoxPageFetcher {
	BoxPage getPage(BoxCredentials credentials, String folderId, long offset, int limit, String... fields);
}
