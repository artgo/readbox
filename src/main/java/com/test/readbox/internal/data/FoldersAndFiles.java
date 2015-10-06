package com.test.readbox.internal.data;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.test.readbox.box.BoxPage;

import lombok.Getter;

@Getter
public class FoldersAndFiles {
	private final Collection<FileInfo> folders = new ConcurrentLinkedQueue<>();
	private final Collection<FileInfo> files = new ConcurrentLinkedQueue<>();

	public void addPage(BoxPage page) {
		folders.addAll(page.getFolders());
		files.addAll(page.getFiles());
	}
}
