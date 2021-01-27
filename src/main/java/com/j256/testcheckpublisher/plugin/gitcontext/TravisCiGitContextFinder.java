package com.j256.testcheckpublisher.plugin.gitcontext;

import org.apache.commons.lang3.StringUtils;

/**
 * Methods that extract the git context from the Travis CI environmental variables.
 * 
 * @author graywatson
 */
public class TravisCiGitContextFinder implements GitContextFinder {

	@Override
	public boolean isRunning() {
		String val = System.getenv("TRAVIS");
		return Boolean.parseBoolean(val);
	}

	@Override
	public GitContext findContext() {

		// TRAVIS_REPO_SLUG: The slug (in form: owner_name/repo_name) of the repository currently being built.
		String slug = System.getenv("TRAVIS_REPO_SLUG");
		if (slug == null) {
			return null;
		}
		String[] parts = StringUtils.split(slug, '/');
		if (parts.length != 2) {
			return null;
		}

		String owner = parts[0];
		String repository = parts[1];
		String commitSha = System.getenv("TRAVIS_COMMIT");
		if (commitSha == null) {
			return null;
		}

		return new GitContext(owner, repository, commitSha);
	}
}
