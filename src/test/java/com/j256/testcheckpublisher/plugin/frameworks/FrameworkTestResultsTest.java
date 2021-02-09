package com.j256.testcheckpublisher.plugin.frameworks;

import static org.junit.Assert.fail;

import org.junit.Test;

public class FrameworkTestResultsTest {

	@Test
	public void testToString() {
		System.out.println(new FrameworkTestResults().toString());
	}
	
	@Test
	public void testFail() {
		fail("Showing a test failure for a test in the commit");
	}
	
	@Test
	public void testDoesntCompile() {
		This doesn't compile;
	}
}
