package com.test.readbox.box;

import java.util.Date;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemInfo {
	private String type;
	private String id;
	private String name;
	@JsonProperty(BoxConstants.CONTENT_MODIFIED_AT_FIELD)
	private Date contentModifiedAt;
}
