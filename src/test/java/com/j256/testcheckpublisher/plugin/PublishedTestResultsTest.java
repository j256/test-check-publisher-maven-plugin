package com.j256.testcheckpublisher.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.j256.testcheckpublisher.plugin.frameworks.FrameworkTestResults;
import com.j256.testcheckpublisher.plugin.frameworks.TestFileResult;
import com.j256.testcheckpublisher.plugin.frameworks.TestFileResult.TestLevel;

public class PublishedTestResultsTest {

	@Test
	public void testCoverage() {
		String owner = "owner";
		String repo = "repo";
		String sha = "sha";
		String secret = "secret";
		String format = "format";
		String frameworkResultsName = "name";
		int numTests = 1;
		int numErrors = 2;
		int numFailures = 3;
		int numSkipped = 4;
		FrameworkTestResults frameworkResults =
				new FrameworkTestResults(frameworkResultsName, numTests, numFailures, numErrors, numSkipped, null);
		PublishedTestResults results = new PublishedTestResults(owner, repo, sha, secret, format, frameworkResults);
		assertEquals(owner, results.getOwner());
		assertEquals(repo, results.getRepository());
		assertEquals(sha, results.getCommitSha());
		assertEquals(secret, results.getSecret());
		assertEquals(format, results.getFormat());
		assertEquals(frameworkResults, results.getResults());
		assertEquals(frameworkResultsName + ": " + numTests + " tests, " + numFailures + " failures, " + numErrors
				+ " errors, " + numSkipped + " skipped, 0 file-results", results.asString());
		assertEquals(PublishedTestResults.MAGIC_VALUE, results.getMagic());
		assertTrue(results.isMagicCorrect());
		results.setMagic(1);
		assertFalse(results.isMagicCorrect());

		results = new PublishedTestResults(owner, repo, sha, secret, format, null);
		assertNull(results.getResults());
		assertEquals("no framework results", results.asString());
	}

	@Test
	public void testToJson() {
		String owner = "owner";
		String repo = "repo";
		String sha = "sha";
		String secret = "secret";
		String format = "format";
		String frameworkResultsName = "name";
		int numTests = 1;
		int numErrors = 2;
		int numFailures = 3;
		int numSkipped = 3;

		TestFileResult fileResult = new TestFileResult("file1.java", 100, 100, TestLevel.ERROR, 0.1F, "testName",
				"test message", "more details here");

		FrameworkTestResults frameworkResults = new FrameworkTestResults(frameworkResultsName, numTests, numFailures,
				numErrors, numSkipped, Arrays.asList(fileResult));
		PublishedTestResults results = new PublishedTestResults(owner, repo, sha, secret, format, frameworkResults);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		System.out.println(gson.toJson(results));
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testFormat() {
		String frameworkResultsName = "name";
		int numTests = 1;
		int numErrors = 2;
		int numFailures = 3;
		int numSkipped = 3;

		TestFileResult fileResult = new TestFileResult("file1.java", 100, 100, TestLevel.ERROR, 0.1F, "testName",
				"test message", "more details here");

		PublishedTestResults results = new PublishedTestResults("owner", "repo", "sha", "secret", null, null);
		assertNull(results.getFormat());

		FrameworkTestResults frameworkResults = new FrameworkTestResults(frameworkResultsName, numTests, numFailures,
				numErrors, numSkipped, Arrays.asList(fileResult));
		results = new PublishedTestResults("owner", "repo", "sha", "secret", null, frameworkResults);
		assertNull(results.getFormat());
		String format = "format";
		frameworkResults.setFormat(format);
		assertEquals(format, results.getFormat());
	}
}
