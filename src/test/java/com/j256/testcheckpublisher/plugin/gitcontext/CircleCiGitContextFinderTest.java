package com.j256.testcheckpublisher.plugin.gitcontext;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class CircleCiGitContextFinderTest {

	@Test
	public void testStuff() {
		CircleCiGitContextFinder circle = new CircleCiGitContextFinder();
		if (circle.isRunning()) {
			assertNotNull(circle.findContext());
		} else {
			assertNull(circle.findContext());
		}
	}
}
