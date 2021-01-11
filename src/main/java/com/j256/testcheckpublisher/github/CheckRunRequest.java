package com.j256.testcheckpublisher.github;

import java.util.ArrayList;
import java.util.Collection;

import com.google.gson.annotations.SerializedName;

public class CheckRunRequest {
	final String name;
	@SerializedName("head_sha")
	final String sha;
	final CheckRunOutput output;

	public CheckRunRequest(String name, String sha, CheckRunOutput output) {
		this.name = name;
		this.sha = sha;
		this.output = output;
	}

	public static class CheckRunOutput {
		String title;
		String summary;
		final String text;
		transient int testCount;
		transient int failureCount;
		transient int errorCount;

		Collection<CheckRunAnnotation> annotations;

		public CheckRunOutput(String title, String summary, String text) {
			this.title = title;
			this.summary = summary;
			this.text = text;
		}

		public Collection<CheckRunAnnotation> getAnnotations() {
			return annotations;
		}

		public void addCounts(int testCount, int failureCount, int errorCount) {
			this.testCount += testCount;
			this.failureCount += failureCount;
			this.errorCount += errorCount;
		}

		public void addAnnotation(CheckRunAnnotation annotation) {
			if (this.annotations == null) {
				this.annotations = new ArrayList<>();
			}
			this.annotations.add(annotation);
			if (annotation.level == Level.FAILURE) {
				failureCount++;
			} else if (annotation.level == Level.ERROR) {
				errorCount++;
			}
		}

		public int getTestCount() {
			return testCount;
		}

		public int getFailureCount() {
			return failureCount;
		}

		public int getErrorCount() {
			return errorCount;
		}

		public void setSummary(String summary) {
			this.summary = summary;
		}

		public void setTitle(String title) {
			this.title = title;
		}
	}

	public static class CheckRunAnnotation {
		String path;
		@SerializedName("start_line")
		int startLine;
		@SerializedName("end_line")
		int endLine;
		@SerializedName("start_column")
		int startColumn;
		@SerializedName("end_column")
		int endColumn;
		@SerializedName("annotation_level")
		Level level;
		String message;
		String title;
		@SerializedName("raw_details")
		String details;

		public CheckRunAnnotation(String path, int startLine, int endLine, Level level, String message, String title,
				String details) {
			this.path = path;
			this.startLine = startLine;
			this.endLine = endLine;
			this.level = level;
			this.message = message;
			this.title = title;
			this.details = details;
		}

		public Level getLevel() {
			return level;
		}
	}

	public static enum Level {
		@SerializedName("notice")
		NOTICE,
		@SerializedName("warning")
		WARNING,
		// serialized as failure but recorded differently
		@SerializedName("failure")
		ERROR,
		@SerializedName("failure")
		FAILURE,
		// end
		;
	}
}
