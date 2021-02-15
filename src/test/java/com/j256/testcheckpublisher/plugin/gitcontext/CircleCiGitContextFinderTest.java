package com.j256.testcheckpublisher.plugin.gitcontext;

import static org.junit.Assert.*;

import org.junit.Test;

public class CircleCiGitContextFinderTest {

	@Test
	public void testStuff() {
		CircleCiGitContextFinder circle = new CircleCiGitContextFinder();
		assertFalse(circle.isRunning());
		assertNull(circle.findContext());
	}
}
