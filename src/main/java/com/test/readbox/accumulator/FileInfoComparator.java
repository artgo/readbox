package com.test.readbox.accumulator;

import java.util.Comparator;

import com.test.readbox.FileInfo;

public class FileInfoComparator implements Comparator<FileInfo> {

	public int compare(FileInfo f1, FileInfo f2) {
		if (f1.getLastModified() == null) {
			if (f2.getLastModified() == null) {
				return f1.getId().equals(f2.getId()) ? 0 : -1;
			} else {
				return 1;
			}
		}
		if (f2.getLastModified() == null) {
			return -1;
		}	
		return -f1.getLastModified().compareTo(f2.getLastModified());
	}
}
