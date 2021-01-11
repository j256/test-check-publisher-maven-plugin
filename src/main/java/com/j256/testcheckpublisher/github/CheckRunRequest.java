package com.j256.testcheckpublisher.github;

import java.util.ArrayList;
import java.util.Collection;

import com.google.gson.annotations.SerializedName;

public class CheckRunRequest {
	final String name;
	@SerializedName("head_sha")
	final String sha;
	final CheckRunOutput output;
	final Status status;
	final Conclusion conclusion;

	public CheckRunRequest(String name, String sha, CheckRunOutput output) {
		this.name = name;
		this.sha = sha;
		this.output = output;
		this.status = Status.COMPLETED;
		if (output.errorCount > 0 || output.failureCount > 0) {
			this.conclusion = Conclusion.FAILURE;
		} else {
			this.conclusion = Conclusion.SUCCESS;
		}
	}

	public static class CheckRunOutput {
		String title;
		String summary;
		String text;
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

		public void setText(String text) {
			this.text = text;
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
		String title;
		String message;
		@SerializedName("raw_details")
		String details;

		public CheckRunAnnotation(String path, int startLine, int endLine, Level level, String title, String message,
				String details) {
			this.path = path;
			this.startLine = startLine;
			this.endLine = endLine;
			this.level = level;
			this.title = title;
			this.message = message;
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

	public static enum Status {
		@SerializedName("queued")
		QUEUED,
		@SerializedName("in_progress")
		IN_PROGRESS,
		@SerializedName("completed")
		COMPLETED,
		// end
		;
	}

	public static enum Conclusion {
		@SerializedName("success")
		SUCCESS,
		@SerializedName("failure")
		FAILURE,
		@SerializedName("neutral")
		NEUTRAL,
		@SerializedName("cancelled")
		CANCELLED,
		@SerializedName("skipped")
		SKIPPED,
		@SerializedName("timed_out")
		TIMED_OUT,
		@SerializedName("action_required")
		ACTION_REQUIRED,
		// end
		;
	}
}
