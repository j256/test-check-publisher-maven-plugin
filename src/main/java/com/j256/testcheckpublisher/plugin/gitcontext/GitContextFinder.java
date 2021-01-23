package com.j256.testcheckpublisher.plugin.gitcontext;

/**
 * Class which finds the git context needed to publish the tests.
 * 
 * @author graywatson
 */
public interface GitContextFinder {

	/**
	 * Return if this context is currently running and should be used.
	 */
	public boolean isRunning();

	/**
	 * Return the local git context.
	 */
	public GitContext findContext() throws Exception;

	/**
	 * State information about the local git repo.
	 */
	public static class GitContext {

		private String owner;
		private String repository;
		private String commitSha;

		public GitContext(String owner, String repository, String commitSha) {
			this.owner = owner;
			this.repository = repository;
			this.commitSha = commitSha;
		}

		public String getOwner() {
			return owner;
		}

		public String getRepository() {
			return repository;
		}

		public String getCommitSha() {
			return commitSha;
		}

		@Override
		public String toString() {
			return "GitContext [owner=" + owner + ", repo=" + repository + ", sha=" + commitSha + "]";
		}
	}
}
