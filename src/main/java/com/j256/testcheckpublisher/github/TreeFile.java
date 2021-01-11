package com.j256.testcheckpublisher.github;

/**
 * File associated with a github tree.
 * 
 * @author graywatson
 */
public class TreeFile {

	String path;
	String type;
	String sha;

	public String getPath() {
		return path;
	}

	public String getType() {
		return type;
	}

	public String getSha() {
		return sha;
	}

	@Override
	public String toString() {
		return path;
	}
}
