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
	private String format;
	private List<TestFileResult> fileResults;

	public FrameworkTestResults() {
		// for gson
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

	public int getNumTests() {
		return numTests;
	}

	public int getNumFailures() {
		return numFailures;
	}

	public int getNumErrors() {
		return numErrors;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * Sort the results and remove any low level results above the max argument.
	 */
	public void limitFileResults(int maxNumResults) {
		// sort and remove results above our limit
		if (fileResults != null) {
			Collections.sort(fileResults);
			for (int i = fileResults.size() - 1; i >= maxNumResults; i--) {
				fileResults.remove(i);
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
}
