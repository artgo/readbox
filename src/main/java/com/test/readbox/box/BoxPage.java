package com.test.readbox.box;

import java.util.ArrayList;
import java.util.Collection;

import lombok.Getter;

import com.test.readbox.internal.data.FileInfo;

@Getter
public class BoxPage {
	private final Collection<FileInfo> folders = new ArrayList<>();
	private final Collection<FileInfo> files = new ArrayList<>();
	private final long total;

	public BoxPage(long total) {
		this.total = total;
	}
}
