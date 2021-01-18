package com.j256.testcheckpublisher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility functions that find the state of our local git repo by environmental variable or other means.
 * 
 * @author graywatson
 */
public class GitStateUtility {

	private static final Pattern REMOTE_ORGIN_URL_PATTERN = Pattern.compile(".*:([^/]+)/(.+)\\.git");
	private static final Pattern LOG_PATTERN = Pattern.compile("commit ([^ ]+) ?.*");

	public static GitState findGitState() {
		GitState state = checkCircleCi();
		if (state != null) {
			return state;
		}

		return gitCommandLine();
	}

	private static GitState checkCircleCi() {
		// also CI
		String val = System.getenv("CIRCLECI");
		if (!Boolean.parseBoolean(val)) {
			return null;
		}

		String repository = System.getenv("CIRCLE_PROJECT_REPONAME");
		String owner = System.getenv("CIRCLE_PROJECT_USERNAME");
		String commitSha = System.getenv("CIRCLE_SHA1");

		return new GitState(owner, repository, commitSha);
	}

	private static GitState gitCommandLine() {

		// git config --get remote.origin.url to get owner, repo
		String[] command = new String[] { "git", "config", "--get", "remote.origin.url" };
		String line = getFirstLineOfCommand(command);
		if (line == null) {
			return null;
		}
		Matcher matcher = REMOTE_ORGIN_URL_PATTERN.matcher(line);
		if (!matcher.matches()) {
			System.err.println("Git config line not in correct format: " + line);
			System.err.println("Command: " + Arrays.toString(command));
			return null;
		}

		String owner = matcher.group(1);
		String repository = matcher.group(1);

		// git log -1 gives the sha
		command = new String[] { "git", "log", "-1" };
		line = getFirstLineOfCommand(command);
		if (line == null) {
			return null;
		}
		matcher = LOG_PATTERN.matcher(line);
		if (!matcher.matches()) {
			System.err.println("Git log command output not in correct format: " + line);
			System.err.println("Command: " + Arrays.toString(command));
			return null;
		}

		String commitSha = matcher.group(1);
		return new GitState(owner, repository, commitSha);
	}

	private static String getFirstLineOfCommand(String[] command) {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(command);
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));) {
				String first = reader.readLine();
				while (reader.readLine() != null) {
					// ignore
				}
				return first;
			}
		} catch (IOException ioe) {
			System.err.println("Problems running command: " + Arrays.toString(command));
			ioe.printStackTrace();
			return null;
		} finally {
			if (process != null) {
				process.destroy();
			}
		}
	}

	/**
	 * State information about the local git repo.
	 */
	public static class GitState {

		private String owner;
		private String repository;
		private String commitSha;

		public GitState(String owner, String repository, String commitSha) {
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
	}
}
