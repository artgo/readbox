package com.test.readbox.accumulator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.testng.annotations.Test;

import com.test.readbox.internal.accumulator.ResultsAccumulator;
import com.test.readbox.internal.data.FileInfo;

@Test
public class ResultsAccumulatorTest {

	public void test_gets_150_max_if_added_all_first_time() {
		long time = new Date().getTime() - 10000000;
		List<FileInfo> list = new ArrayList<>();
		for(int i = 0; i < 1000; i++) {
			FileInfo f = new FileInfo("id" + i, "parentId", "name" + i, new Date(time - 1000 * i));
			list.add(f);
		}
		ResultsAccumulator resultsAccumulator = new ResultsAccumulator();
		resultsAccumulator.addAll(list);
		Collection<FileInfo> result = resultsAccumulator.getResults();
		assertThat(result).hasSize(150);
	}

	public void test_keeps_top_150_first_time() {
		long time = new Date().getTime() - 10000000;
		List<FileInfo> list = new ArrayList<>();
		for(int i = 0; i < 1000; i++) {
			FileInfo f = new FileInfo("id" + i, "parentId", "name" + i, new Date(time - 1000 * i));
			list.add(f);
		}
		ResultsAccumulator resultsAccumulator = new ResultsAccumulator();
		resultsAccumulator.addAll(list);
		Collection<FileInfo> result = resultsAccumulator.getResults();
		Iterator<FileInfo> iter = result.iterator();
		assertThat(iter.next().getId()).isEqualTo("id0");
		assertThat(iter.hasNext()).isTrue();
		assertThat(iter.next().getId()).isEqualTo("id1");
		assertThat(iter.hasNext()).isTrue();
		assertThat(iter.next().getId()).isEqualTo("id2");
		assertThat(iter.hasNext()).isTrue();
	}

	public void test_gets_150_max_if_added_all_twice() {
		long time = new Date().getTime() - 10000000;
		List<FileInfo> list = new ArrayList<>();
		for(int i = 0; i < 1000; i++) {
			FileInfo f = new FileInfo("id" + i, "parentId", "name" + i, new Date(time - 1000 * i));
			list.add(f);
		}
		ResultsAccumulator resultsAccumulator = new ResultsAccumulator();
		resultsAccumulator.addAll(list);
		List<FileInfo> list2 = new ArrayList<>();
		for(int i = 1000; i < 2000; i++) {
			FileInfo f = new FileInfo("id" + i, "parentId", "name" + i, new Date(time - 1000 * i));
			list2.add(f);
		}
		resultsAccumulator.addAll(list2);
		Collection<FileInfo> result = resultsAccumulator.getResults();
		assertThat(result).hasSize(150);
	}

	public void test_keeps_top_150_added_twice() {
		long time = new Date().getTime() - 10000000;
		List<FileInfo> list = new ArrayList<>();
		for(int i = 0; i < 1000; i++) {
			FileInfo f = new FileInfo("id" + i, "parentId", "name" + i, new Date(time - 1000 * i));
			list.add(f);
		}
		ResultsAccumulator resultsAccumulator = new ResultsAccumulator();
		resultsAccumulator.addAll(list);
		List<FileInfo> list2 = new ArrayList<>();
		for(int i = 1000; i < 2000; i++) {
			FileInfo f = new FileInfo("id" + i, "parentId", "name" + i, new Date(time - 1000 * i));
			list2.add(f);
		}
		resultsAccumulator.addAll(list2);
		Collection<FileInfo> result = resultsAccumulator.getResults();
		Iterator<FileInfo> iter = result.iterator();
		assertThat(iter.next().getId()).isEqualTo("id0");
		assertThat(iter.hasNext()).isTrue();
		assertThat(iter.next().getId()).isEqualTo("id1");
		assertThat(iter.hasNext()).isTrue();
		assertThat(iter.next().getId()).isEqualTo("id2");
		assertThat(iter.hasNext()).isTrue();
	}

	public void test_keeps_top_150_added_twice_reversed() {
		long time = new Date().getTime() - 10000000;
		List<FileInfo> list = new ArrayList<>();
		for(int i = 0; i < 1000; i++) {
			FileInfo f = new FileInfo("id" + i, "parentId", "name" + i, new Date(time - 1000 * i));
			list.add(f);
		}
		ResultsAccumulator resultsAccumulator = new ResultsAccumulator();
		resultsAccumulator.addAll(list);
		List<FileInfo> list2 = new ArrayList<>();
		for(int i = 1000; i < 2000; i++) {
			FileInfo f = new FileInfo("id" + i, "parentId", "name" + i, new Date(time + 1000 * i));
			list2.add(f);
		}
		resultsAccumulator.addAll(list2);
		Collection<FileInfo> result = resultsAccumulator.getResults();
		Iterator<FileInfo> iter = result.iterator();
		assertThat(iter.next().getId()).isEqualTo("id1999");
		assertThat(iter.hasNext()).isTrue();
		assertThat(iter.next().getId()).isEqualTo("id1998");
		assertThat(iter.hasNext()).isTrue();
		assertThat(iter.next().getId()).isEqualTo("id1997");
		assertThat(iter.hasNext()).isTrue();
	}
}
