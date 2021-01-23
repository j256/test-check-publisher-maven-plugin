package com.j256.testcheckpublisher.plugin.gitcontext;

/**
 * Utility functions that find the state of our local git repo by environmental variable or other means.
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
		String repository = System.getenv("CIRCLE_PROJECT_REPONAME");
		String commitSha = System.getenv("CIRCLE_SHA1");

		return new GitContext(owner, repository, commitSha);
	}
}
