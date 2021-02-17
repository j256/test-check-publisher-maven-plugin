package com.j256.testcheckpublisher.plugin.gitcontext;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.j256.testcheckpublisher.plugin.gitcontext.GitContextFinder.GitContext;

public class TravisCiGitContextFinderTest {

	@Test
	public void testStuff() {
		TravisCiGitContextFinder travis = new TravisCiGitContextFinder();
		assertFalse(travis.isRunning());
		GitContext gitContext = travis.findContext();
		assertNull(gitContext);
	}

}
