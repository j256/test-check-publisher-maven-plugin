package com.j256.testcheckpublisher.plugin;

import com.j256.testcheckpublisher.plugin.frameworks.FrameworkTestResults;

/**
 * Results from the test framework with header information.
 * 
 * @author graywatson
 */
public class PublishedTestResults {

	public static final long MAGIC_VALUE = 237347409389423823L;

	private long magic;
	private String owner;
	private String repository;
	private String commitSha;
	private String secret;
	// can be null if it is in the results because of backwards compatibility
	private String format;
	private FrameworkTestResults results;

	public PublishedTestResults(String owner, String repository, String commitSha, String secret, String format,
			FrameworkTestResults results) {
		this.magic = MAGIC_VALUE;
		this.owner = owner;
		this.repository = repository;
		this.commitSha = commitSha;
		this.secret = secret;
		this.format = format;
		this.results = results;
	}

	public long getMagic() {
		return magic;
	}

	/**
	 * For testing.
	 */
	void setMagic(long magic) {
		this.magic = magic;
	}

	public boolean isMagicCorrect() {
		return (magic == MAGIC_VALUE);
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

	public String getSecret() {
		return secret;
	}

	@SuppressWarnings("deprecation")
	public String getFormat() {
		if (format != null) {
			return format;
		} else if (results == null) {
			return null;
		} else {
			return results.getFormat();
		}
	}

	public String asString() {
		if (results == null) {
			return "no framework results";
		} else {
			return results.asString();
		}
	}

	public FrameworkTestResults getResults() {
		return results;
	}
}
