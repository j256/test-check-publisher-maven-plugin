package com.j256.testcheckpublisher.plugin.frameworks;

/**
 * File result associated with a specific test.
 */
public class TestFileResult implements Comparable<TestFileResult> {

	private final String path;
	@Deprecated
	private int lineNumber;
	private final int startLineNumber;
	private final int endLineNumber;
	private final TestLevel testLevel;
	private final float timeSeconds;
	private final String testName;
	private final String message;
	private final String details;

	public TestFileResult(String path, int startLineNumber, int endLineNumber, TestLevel testLevel, float timeSeconds,
			String testName, String message, String details) {
		this.path = path;
		this.startLineNumber = startLineNumber;
		this.endLineNumber = endLineNumber;
		this.testLevel = testLevel;
		this.timeSeconds = timeSeconds;
		this.testName = testName;
		this.message = message;
		this.details = details;
	}

	public String getPath() {
		return path;
	}

	public int getStartLineNumber() {
		if (startLineNumber == 0) {
			return lineNumber;
		} else {
			return startLineNumber;
		}
	}

	public int getEndLineNumber() {
		if (endLineNumber == 0) {
			return lineNumber;
		} else {
			return endLineNumber;
		}
	}

	/**
	 * For testing purposes.
	 */
	void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
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
		if (other.testLevel == null) {
			if (testLevel == null) {
				return 0;
			} else {
				return -1;
			}
		} else if (testLevel == null) {
			return 1;
		}
		int result = other.testLevel.compareValue(testLevel);
		if (result != 0) {
			return result;
		} else if (path == null) {
			if (other.path == null) {
				return 0;
			} else {
				return -1;
			}
		} else if (other.path == null) {
			return 1;
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
		if (path == null) {
			if (other.path != null) {
				return false;
			}
		} else if (!path.equals(other.path)) {
			return false;
		}
		if (lineNumber != other.lineNumber) {
			return false;
		}
		if (testLevel != other.testLevel) {
			return false;
		}
		if (Float.floatToIntBits(timeSeconds) != Float.floatToIntBits(other.timeSeconds)) {
			return false;
		}
		if (testName == null) {
			if (other.testName != null) {
				return false;
			}
		} else if (!testName.equals(other.testName)) {
			return false;
		}
		if (message == null) {
			if (other.message != null) {
				return false;
			}
		} else if (!message.equals(other.message)) {
			return false;
		}
		if (details == null) {
			if (other.details != null) {
				return false;
			}
		} else if (!details.equals(other.details)) {
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
		NOTICE(1, "Notice"),
		FAILURE(2, "Failure"),
		ERROR(3, "Error"),
		// end
		;

		private final int value;
		private final String prettyString;

		private TestLevel(int value, String prettyString) {
			this.value = value;
			this.prettyString = prettyString;
		}

		public int compareValue(TestLevel other) {
			return value - other.value;
		}

		public String getPrettyString() {
			return prettyString;
		}

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
}
