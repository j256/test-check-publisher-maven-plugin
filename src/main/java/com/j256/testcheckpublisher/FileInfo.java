package com.j256.testcheckpublisher;

import java.io.File;

/**
 * Information about a particular file.
 * 
 * @author graywatson
 */
public class FileInfo {

	final String path;
	final String name;
	final String sha;

	public FileInfo(String path, String sha) {
		this.path = path;
		// extract our file-name
		int index = path.lastIndexOf(File.separatorChar);
		if (index < path.length() - 1) {
			this.name = path.substring(index + 1);
		} else {
			this.name = path;
		}
		this.sha = sha;
	}

	public String getPath() {
		return path;
	}

	public String getName() {
		return name;
	}

	public String getSha() {
		return sha;
	}

	@Override
	public String toString() {
		return path;
	}
}
