package com.j256.testcheckpublisher.plugin;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;

import com.j256.testcheckpublisher.plugin.gitcontext.GitContextFinderType;

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

	@Test(expected = IllegalStateException.class)
	public void testBadEnd() throws MojoExecutionException {
		TestCheckPubMojo mojo = new TestCheckPubMojo();
		mojo.setSecretEnvName("unknown env name");
		ResultPoster poster = createMock(ResultPoster.class);
		mojo.setResultPoster(poster);

		mojo.setThrowOnError(true);
		mojo.execute();
	}

	@Test
	public void testBadContextFinder() throws MojoExecutionException, IOException {
		TestCheckPubMojo mojo = new TestCheckPubMojo();
		mojo.setContext(GitContextFinderType.TRAVIS_CI);
		ResultPoster poster = createMock(ResultPoster.class);
		mojo.setResultPoster(poster);
		mojo.setSecretValue("foo");

		poster.postResults(isA(PublishedTestResults.class));

		mojo.setThrowOnError(true);
		replay(poster);
		mojo.execute();
		verify(poster);
	}
}
