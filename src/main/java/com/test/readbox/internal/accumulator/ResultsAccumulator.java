package com.test.readbox.internal.accumulator;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import com.test.readbox.internal.data.FileInfo;

public class ResultsAccumulator {
	private static final int FILES_TO_RETAIN = 150;

	private final ConcurrentSkipListSet<FileInfo> results = new ConcurrentSkipListSet<>(new FileInfoComparator());

	public void addAll(Collection<FileInfo> files) {
		if (hasDateLimit()) {
			filteredAdd(files);
		} else {
			results.addAll(files);
		}
		refreshDateLimit();
	}

	private void filteredAdd(Collection<FileInfo> files) {
		Date limit = getDateLimit();
		results.addAll(files.stream().filter(f -> limit.before(f.getLastModified())).collect(Collectors.toList()));
	}

	private void refreshDateLimit() {
		if (results.size() > FILES_TO_RETAIN) {
			int counter = FILES_TO_RETAIN;
			for (FileInfo fileInfo : results) {
				if (--counter <= 0) {
					results.removeAll(results.tailSet(fileInfo, false));
					break;
				}
			}
		}
	}

	public Collection<FileInfo> getResults() {
		refreshDateLimit();
		return results;
	}

	public boolean hasDateLimit() {
		return results.size() >= FILES_TO_RETAIN;
	}

	public Date getDateLimit() {
		return results.last().getLastModified();
	}
}
