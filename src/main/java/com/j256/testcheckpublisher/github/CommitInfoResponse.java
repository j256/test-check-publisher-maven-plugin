package com.j256.testcheckpublisher.github;

public class CommitInfoResponse {

	Tree tree;

	public Tree getTree() {
		return tree;
	}

	public static class Tree {
		String sha;

		public String getSha() {
			return sha;
		}
	}
}
