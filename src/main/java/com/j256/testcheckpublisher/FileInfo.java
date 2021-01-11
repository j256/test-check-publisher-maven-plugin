package com.j256.testcheckpublisher;

/**
 * Information about a particular file.
 * 
 * @author graywatson
 */
public class FileInfo {

	final String path;
	final String name;
	final String sha;

	public FileInfo(String path, String name, String sha) {
		this.path = path;
		this.name = name;
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
