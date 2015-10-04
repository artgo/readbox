package com.test.readbox.accumulator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.concurrent.ConcurrentSkipListSet;

import org.joda.time.DateTime;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.test.readbox.FileInfo;

@Test
public class FileInfoComparatorTest {
	private FileInfoComparator comparator;

	@BeforeClass(alwaysRun = true)
	public void setup() {
		comparator = new FileInfoComparator();
	}

	public void test_reverse_comparison_in_days() {
		DateTime dt1 = new DateTime(2015, 4, 7, 12, 0, 0, 0);
		DateTime dt2 = new DateTime(2015, 4, 8, 12, 0, 0, 0);
		Date d1 = dt1.toDate();
		Date d2 = dt2.toDate();
		FileInfo f1 = new FileInfo("id", "parentId", "name", d1);
		FileInfo f2 = new FileInfo("id2", "parentId", "name2", d2);
		assertThat(comparator.compare(f1, f2)).isEqualTo(-d1.compareTo(d2));
	}

	public void test_reverse_comparison_in_seconds() {
		DateTime dt1 = new DateTime(2015, 4, 7, 12, 0, 0, 0);
		DateTime dt2 = new DateTime(2015, 4, 7, 12, 0, 0, 1);
		Date d1 = dt1.toDate();
		Date d2 = dt2.toDate();
		FileInfo f1 = new FileInfo("id", "parentId", "name", d1);
		FileInfo f2 = new FileInfo("id2", "parentId", "name2", d2);
		assertThat(comparator.compare(f1, f2)).isEqualTo(-d1.compareTo(d2));
	}

	public void test_reverse_sort_by_sortedset() {
		DateTime dt1 = new DateTime(2015, 4, 7, 12, 0, 0, 0);
		DateTime dt2 = new DateTime(2015, 4, 8, 12, 0, 0, 0);
		DateTime dt3 = new DateTime(2015, 4, 9, 12, 0, 0, 0);
		Date d1 = dt1.toDate();
		Date d2 = dt2.toDate();
		Date d3 = dt3.toDate();
		FileInfo f1 = new FileInfo("id", "parentId", "name", d1);
		FileInfo f2 = new FileInfo("id2", "parentId", "name2", d2);
		FileInfo f3 = new FileInfo("id3", "parentId", "name3", d3);
		ConcurrentSkipListSet<FileInfo> set = new ConcurrentSkipListSet<>(new FileInfoComparator());
		set.add(f2);
		set.add(f1);
		set.add(f3);
		assertThat(set).hasSize(3);
		assertThat(set.first()).isEqualTo(f3);
		assertThat(set.last()).isEqualTo(f1);
		assertThat(set.toArray()).containsSequence(f3, f2, f1);
	}
}
