package com.j256.testcheckpublisher.plugin;

import java.io.IOException;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;

public class ServerResultPosterTest {

	@Test
	public void testStuff() throws IOException {
		ServerResultPoster poster = new ServerResultPoster("https://256stuff.com/", new SystemStreamLog());
		PublishedTestResults results = new PublishedTestResults("onwer", "repo", "sha", "secret", "format", null);
		poster.postResults(results);
	}

	@Test(expected = IOException.class)
	public void testBadAddress() throws IOException {
		ServerResultPoster poster = new ServerResultPoster("http://localhost:9199/", new SystemStreamLog());
		PublishedTestResults results = new PublishedTestResults("onwer", "repo", "sha", "secret", "format", null);
		poster.postResults(results);
	}
}
