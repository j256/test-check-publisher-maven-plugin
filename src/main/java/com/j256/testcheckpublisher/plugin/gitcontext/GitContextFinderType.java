package com.j256.testcheckpublisher.plugin.gitcontext;

import org.apache.maven.plugin.logging.Log;

import com.j256.testcheckpublisher.plugin.gitcontext.GitContextFinder.GitContext;

/**
 * Types of context finders that we support.
 */
public enum GitContextFinderType {
	CIRCLE_CI(new CircleCiGitContextFinder()),
	// this should be at the end because it is always available
	GIT_COMMAND(new CommandLineGitContextFinder()),
	// end
	;

	private final GitContextFinder contextFinder;

	private GitContextFinderType(GitContextFinder contextFinder) {
		this.contextFinder = contextFinder;
	}

	public boolean isRunning() {
		return contextFinder.isRunning();
	}

	public static GitContextFinderType getDefault() {
		return GIT_COMMAND;
	}

	public GitContext findContext(Log log) {
		try {
			return contextFinder.findContext();
		} catch (Exception e) {
			log.error("Finding context threw with: " + this, e);
			return null;
		}
	}
}
