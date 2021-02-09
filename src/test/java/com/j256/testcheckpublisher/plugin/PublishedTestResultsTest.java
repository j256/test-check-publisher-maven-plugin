package com.j256.testcheckpublisher.plugin;

import static org.junit.Assert.*;

import org.junit.Test;

import com.j256.testcheckpublisher.plugin.frameworks.FrameworkTestResults;

public class PublishedTestResultsTest {

	@Test
	public void testCoverage() {
		String owner = "owner";
		String repo = "repo";
		String sha = "sha";
		String secret = "secret";
		String frameworkResultsName = "name";
		int numTests = 1;
		int numErrors = 2;
		int numFailures = 3;
		FrameworkTestResults frameworkResults =
				new FrameworkTestResults(frameworkResultsName, numTests, numFailures, numErrors, null, "format");
		PublishedTestResults results = new PublishedTestResults(owner, repo, sha, secret, frameworkResults);
		assertEquals(owner, results.getOwner());
		assertEquals(repo, results.getRepository());
		assertEquals(sha, results.getCommitSha());
		assertEquals(secret, results.getSecret());
		assertEquals(frameworkResults, results.getResults());
		assertEquals(frameworkResultsName + ": " + numTests + " tests, " + numFailures + " failures, " + numErrors
				+ " errors, 0 file-results", results.asString());
		assertEquals(PublishedTestResults.MAGIC_VALUE, results.getMagic());
		assertTrue(results.isMagicCorrect());
		results.setMagic(1);
		assertFalse(results.isMagicCorrect());

		results = new PublishedTestResults(owner, repo, sha, secret, null);
		assertNull(results.getResults());
		assertEquals("no framework results", results.asString());
	}
}
