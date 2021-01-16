package com.j256.testcheckpublisher.github;

public class GitCommitInfoResponse {

	String sha;
	Tree tree;
	ChangedFile[] files;

	public String getSha() {
		return sha;
	}

	public Tree getTree() {
		return tree;
	}

	public static class Tree {
		String sha;

		public String getSha() {
			return sha;
		}
	}

	public static class ChangedFile {
		String filename;
		// added, removed, modified, renamed
		String status;
	}
}
