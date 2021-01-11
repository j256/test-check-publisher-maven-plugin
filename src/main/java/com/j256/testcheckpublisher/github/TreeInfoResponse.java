package com.j256.testcheckpublisher.github;

import com.google.gson.annotations.SerializedName;

public class TreeInfoResponse {

	String sha;
	@SerializedName("tree")
	TreeFile[] treeFiles;

	public String getSha() {
		return sha;
	}

	public TreeFile[] getTreeFiles() {
		return treeFiles;
	}
}
