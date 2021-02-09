package com.j256.testcheckpublisher.plugin;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;

public class TestCheckPubMojoTest {

	@Test
	public void testStuff() throws MojoExecutionException, IOException {
		TestCheckPubMojo mojo = new TestCheckPubMojo();
		ResultPoster poster = createMock(ResultPoster.class);
		mojo.setResultPoster(poster);
		mojo.setSecretValue("foo");

		poster.postResults(isA(PublishedTestResults.class));

		replay(poster);
		mojo.execute();
		verify(poster);
	}
}
