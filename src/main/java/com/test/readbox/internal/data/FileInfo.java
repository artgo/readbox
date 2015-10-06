package com.test.readbox.internal.data;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Immutable file info
 *
 * id: “file-id”, parentId: “parent-folder-id”, name: “file-name”, lastModified: “last-modified-date”
 *
 */
@Getter @ToString @AllArgsConstructor @Builder @EqualsAndHashCode(of = "id")
public class FileInfo {
	private final String id;
	private final String parentId;
	private final String name;
	private final Date lastModified;
}
