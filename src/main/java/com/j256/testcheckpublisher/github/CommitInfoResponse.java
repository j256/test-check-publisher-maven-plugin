package com.j256.testcheckpublisher.github;

public class CommitInfoResponse {

	String sha;
	Commit commit;
	ChangedFile[] files;

	public String getSha() {
		return sha;
	}

	public Commit getCommit() {
		return commit;
	}

	public ChangedFile[] getFiles() {
		return files;
	}

	public String getTreeSha() {
		if (commit == null || commit.tree == null) {
			return null;
		} else {
			return commit.tree.sha;
		}
	}

	public static class Commit {
		Tree tree;

		public Tree getTree() {
			return tree;
		}
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

		public String getFilename() {
			return filename;
		}

		public String getStatus() {
			return status;
		}

		@Override
		public String toString() {
			return filename;
		}
	}
}
