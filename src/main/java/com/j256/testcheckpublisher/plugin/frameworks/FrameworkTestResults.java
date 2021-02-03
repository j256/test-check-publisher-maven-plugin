package com.j256.testcheckpublisher.plugin.frameworks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
	private List<TestFileResult> fileResults;
	private String format;

	public FrameworkTestResults() {
	}

	public FrameworkTestResults(String name, int numTests, int numFailures, int numErrors,
			List<TestFileResult> fileResults, String format) {
		this.name = name;
		this.numTests = numTests;
		this.numFailures = numFailures;
		this.numErrors = numErrors;
		this.fileResults = fileResults;
		this.format = format;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
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

	/**
	 * Sort the results and remove any low level results above the max argument.
	 */
	public void limitFileResults(int maxNumResults) {
		// sort and remove results above our limit
		if (fileResults != null) {
			Collections.sort(fileResults);
			for (int i = fileResults.size() - 1; i >= maxNumResults; i--) {
				fileResults.remove(fileResults.size() - 1);
			}
		}
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
		this.fileResults.add(result);
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
				+ ", numErrors=" + numErrors + ", numFileResults=" + (fileResults == null ? 0 : fileResults.size())
				+ "]";
	}

	/**
	 * File result associated with a specific test.
	 */
	public static class TestFileResult implements Comparable<TestFileResult> {

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

		@Override
		public int compareTo(TestFileResult other) {
			// we want higher levels to come first
			int result = other.testLevel.compareValue(testLevel);
			if (result != 0) {
				return result;
			} else {
				return path.compareTo(other.path);
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = prime + ((details == null) ? 0 : details.hashCode());
			result = prime * result + lineNumber;
			result = prime * result + ((message == null) ? 0 : message.hashCode());
			result = prime * result + ((path == null) ? 0 : path.hashCode());
			result = prime * result + ((testLevel == null) ? 0 : testLevel.hashCode());
			result = prime * result + ((testName == null) ? 0 : testName.hashCode());
			result = prime * result + Float.floatToIntBits(timeSeconds);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			TestFileResult other = (TestFileResult) obj;
			if (details == null) {
				if (other.details != null) {
					return false;
				}
			} else if (!details.equals(other.details)) {
				return false;
			}
			if (lineNumber != other.lineNumber) {
				return false;
			}
			if (message == null) {
				if (other.message != null) {
					return false;
				}
			} else if (!message.equals(other.message)) {
				return false;
			}
			if (path == null) {
				if (other.path != null) {
					return false;
				}
			} else if (!path.equals(other.path)) {
				return false;
			}
			if (testLevel != other.testLevel) {
				return false;
			}
			if (testName == null) {
				if (other.testName != null) {
					return false;
				}
			} else if (!testName.equals(other.testName)) {
				return false;
			}
			if (Float.floatToIntBits(timeSeconds) != Float.floatToIntBits(other.timeSeconds)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "TestFileResult [path=" + path + ", line=" + lineNumber + ", level=" + testLevel + ", time="
					+ timeSeconds + ", test" + testName + ", message=" + message + ", details=" + details + "]";
		}

		/**
		 * Level of the file-test result.
		 */
		public static enum TestLevel {
			NOTICE(1),
			FAILURE(2),
			ERROR(3),
			// end
			;

			private final int value;

			private TestLevel(int value) {
				this.value = value;
			}

			public int compareValue(TestLevel other) {
				return value - other.value;
			}

			@Override
			public String toString() {
				return name().toLowerCase();
			}
		}
	}
}
