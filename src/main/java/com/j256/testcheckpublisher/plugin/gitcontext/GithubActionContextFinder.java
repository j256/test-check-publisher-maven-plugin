package com.j256.testcheckpublisher.plugin.gitcontext;

/**
 * Methods that extract the git context from the Circle CI environmental variables.
 * 
 * @author graywatson
 */
public class GithubActionContextFinder implements GitContextFinder {

	/*
	 * Variables read from the environment that may be overridden for tests.
	 */
	private String githubAction = System.getenv("GITHUB_ACTION");
	private String githubRepositoryOwner = System.getenv("GITHUB_REPOSITORY_OWNER");
	/** repository == really owner/repository */
	private String githubOwnerAndRepository = System.getenv("GITHUB_REPOSITORY");
	private String githubSha = System.getenv("GITHUB_SHA");

	@Override
	public boolean isRunning() {
		return (githubAction != null && !githubAction.isEmpty());
	}

	@Override
	public GitContext findContext() {

		if (githubRepositoryOwner == null) {
			return null;
		}
		if (githubOwnerAndRepository == null) {
			return null;
		}
		int index = githubOwnerAndRepository.indexOf('/');
		if (index < 0) {
			return null;
		}
		String repository = githubOwnerAndRepository.substring(index + 1);
		if (repository.isEmpty()) {
			return null;
		}
		if (githubSha == null) {
			return null;
		}

		return new GitContext(githubRepositoryOwner, repository, githubSha);
	}

	public void setGithubAction(String githubAction) {
		this.githubAction = githubAction;
	}

	public void setGithubRepositoryOwner(String githubRepositoryOwner) {
		this.githubRepositoryOwner = githubRepositoryOwner;
	}

	public void setGithubOwnerAndRepository(String githubOwnerAndRepository) {
		this.githubOwnerAndRepository = githubOwnerAndRepository;
	}

	public void setGithubSha(String githubSha) {
		this.githubSha = githubSha;
	}
}
