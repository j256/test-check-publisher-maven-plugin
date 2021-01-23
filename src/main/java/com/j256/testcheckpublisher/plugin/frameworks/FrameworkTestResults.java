package com.j256.testcheckpublisher.plugin.frameworks;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Results from the test framework.
 * 
 * @author graywatson
 */
public class FrameworkTestResults {

	private String name;
	private int numTests;
	private int numFailures;
	private int numErrors;
	private Collection<TestFileResult> fileResults;
	private final transient int maxNumResults;

	public FrameworkTestResults(int maxNumResults) {
		this.maxNumResults = maxNumResults;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNumTests() {
		return numTests;
	}

	public int getNumFailures() {
		return numFailures;
	}

	public int getNumErrors() {
		return numErrors;
	}

	public Collection<TestFileResult> getFileResults() {
		return fileResults;
	}

	public void addCounts(int numTests, int numFailures, int numErrors) {
		this.numTests += numTests;
		this.numFailures += numFailures;
		this.numErrors += numErrors;
	}

	public void addFileResult(TestFileResult result) {
		if (this.fileResults == null) {
			this.fileResults = new ArrayList<>();
		}
		if (this.fileResults.size() < maxNumResults) {
			this.fileResults.add(result);
		}
	}

	/**
	 * Pretty print our results for logging.
	 */
	public String asString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append(": ");
		sb.append(numTests).append(" tests, ");
		// failures all of the time
		sb.append(numFailures).append(" failures, ");
		// errors only if there are some
		if (numErrors > 0) {
			sb.append(numErrors).append(" errors, ");
		}
		if (fileResults == null) {
			sb.append('0');
		} else {
			sb.append(fileResults.size());
		}
		sb.append(" file-results");
		return sb.toString();
	}

	@Override
	public String toString() {
		return "FrameworkTestResults [name=" + name + ", numTests=" + numTests + ", numFailures=" + numFailures
				+ ", numErrors=" + numErrors + ", numFileResults=" + fileResults.size() + "]";
	}

	/**
	 * File result associated with a specific test.
	 */
	public static class TestFileResult {

		private final String path;
		private final int lineNumber;
		private final TestLevel testLevel;
		private final float timeSeconds;
		private final String testName;
		private final String message;
		private final String details;

		public TestFileResult(String path, int lineNumber, TestLevel testLevel, float timeSeconds, String testName,
				String message, String details) {
			this.path = path;
			this.lineNumber = lineNumber;
			this.testLevel = testLevel;
			this.timeSeconds = timeSeconds;
			this.testName = testName;
			this.message = message;
			this.details = details;
		}

		public String getPath() {
			return path;
		}

		public int getLineNumber() {
			return lineNumber;
		}

		public TestLevel getTestLevel() {
			return testLevel;
		}

		public float getTimeSeconds() {
			return timeSeconds;
		}

		public String getTestName() {
			return testName;
		}

		public String getMessage() {
			return message;
		}

		public String getDetails() {
			return details;
		}

		/**
		 * Level of the file-test result.
		 */
		public static enum TestLevel {
			NOTICE,
			FAILURE,
			ERROR,
			// end
			;

			@Override
			public String toString() {
				return name().toLowerCase();
			}
		}
	}
}
