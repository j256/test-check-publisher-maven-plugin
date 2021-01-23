package com.j256.testcheckpublisher.gitcontext;

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
public class CommandLineGitContextFinder implements GitContextFinder {

	private static final Pattern REMOTE_ORGIN_URL_PATTERN = Pattern.compile(".*:([^/]+)/(.+)\\.git");
	private static final Pattern LOG_PATTERN = Pattern.compile("commit ([^ ]+) ?.*");

	@Override
	public boolean isRunning() {
		return true;
	}

	@Override
	public GitContext findContext() {

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
		String repository = matcher.group(2);

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
		return new GitContext(owner, repository, commitSha);
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
}
