package com.test.readbox;


public interface BoxFetcher {
	public FoldersAndFiles fetch(String folderId) throws InterruptedException;
}
