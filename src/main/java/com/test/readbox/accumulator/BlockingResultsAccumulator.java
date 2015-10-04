package com.test.readbox.accumulator;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.stream.Collectors;

import com.google.inject.Singleton;
import com.test.readbox.FileInfo;

@Singleton
public class BlockingResultsAccumulator {
	private static final int FILES_TO_RETAIN = 150;

	//private final ConcurrentSkipListSet<FileInfo> results = new ConcurrentSkipListSet<>(new FileInfoComparator());
	private final TreeSet<FileInfo> results = new TreeSet<>(new FileInfoComparator());
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final WriteLock writeLock = lock.writeLock();
	private final ReadLock readLock = lock.readLock();

	public void addAll(Collection<FileInfo> files) {
		writeLock.lock();
		try {
			if (hasDateLimit()) {
				filteredAdd(files);
			} else {
				results.addAll(files);
			}
			refreshDateLimit();
		} finally {
			writeLock.unlock();
		}
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
					Set<FileInfo> toRemove = new HashSet<FileInfo>(results.tailSet(fileInfo, false));
					results.removeAll(toRemove);
					break;
				}
			}
		}
	}

	public Collection<FileInfo> getResults() {
		writeLock.lock();
		try {
			refreshDateLimit();
		} finally {
			writeLock.unlock();
		}
		return results;
	}

	public boolean hasDateLimit() {
		readLock.lock();
		try {
			return results.size() >= FILES_TO_RETAIN;
		} finally {
			readLock.unlock();
		}
	}

	public Date getDateLimit() {
		readLock.lock();
		try {
			return results.last().getLastModified();
		} finally {
			readLock.unlock();
		}
	}
}
