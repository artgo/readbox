package com.test.readbox;

import java.util.ArrayList;
import java.util.Collection;

public class FoldersAndFiles {
	private final Collection<FileInfo> folders = new ArrayList<>();
	private final Collection<FileInfo> files = new ArrayList<>();
	
	public Collection<FileInfo> getFolders() {
		return folders;
	}
	
	public Collection<FileInfo> getFiles() {
		return files;
	}
}
