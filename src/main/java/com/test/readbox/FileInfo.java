package com.test.readbox;

import java.util.Date;

/**
 * Immutable file info
 * 
 * id: “file-id”, parentId: “parent-folder-id”, name: “file-name”, lastModified: “last-modified-date”
 *
 */
public class FileInfo {
	private final String id;
	private final String parentId;
	private final String name;
	private final Date lastModified;

	public FileInfo(String id, String parentId, String name, Date lastModified) {
		super();
		this.id = id;
		this.parentId = parentId;
		this.name = name;
		this.lastModified = lastModified;
	}

	public String getId() {
		return id;
	}

	public String getParentId() {
		return parentId;
	}

	public String getName() {
		return name;
	}

	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public int hashCode() {
		return (id == null) ? 0 : id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		FileInfo other = (FileInfo) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "FileInfo [id=" + id + ", parentId=" + parentId + ", name=" + name + ", lastModified=" + lastModified + "]";
	}
}
