package com.test.readbox.box;

import java.util.List;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageListResult {
	@JsonProperty("total_count")
	private long total;
	private List<ItemInfo> entries;
}
