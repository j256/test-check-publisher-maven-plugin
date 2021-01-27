package com.j256.testcheckpublisher.plugin.gitcontext;

/**
 * Methods that extract the git context from the Circle CI environmental variables.
 * 
 * @author graywatson
 */
public class CircleCiGitContextFinder implements GitContextFinder {

	@Override
	public boolean isRunning() {
		String val = System.getenv("CIRCLECI");
		return Boolean.parseBoolean(val);
	}

	@Override
	public GitContext findContext() {

		String owner = System.getenv("CIRCLE_PROJECT_USERNAME");
		if (owner == null) {
			return null;
		}
		String repository = System.getenv("CIRCLE_PROJECT_REPONAME");
		if (repository == null) {
			return null;
		}
		String commitSha = System.getenv("CIRCLE_SHA1");
		if (commitSha == null) {
			return null;
		}

		return new GitContext(owner, repository, commitSha);
	}
}
