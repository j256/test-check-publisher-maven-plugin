package com.j256.testcheckpublisher.plugin.frameworks;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.j256.testcheckpublisher.plugin.frameworks.SurefireTestSuite.Problem;
import com.j256.testcheckpublisher.plugin.frameworks.SurefireTestSuite.TestCase;

public class SurefireTestSuiteTest {

	@Test
	public void testCoverage() {
		String name = "name";
		float timeSecs = 0.1F;
		int numTests = 10;
		int numErrors = 1;
		int numFailures = 2;

		String className = "classname";
		String message = "message";
		String type = "type";
		String body1 = "body1";
		String body2 = "body2";
		Problem error = new Problem(message, type, body1);
		Problem failure = new Problem(message, type, body2);
		TestCase testCase = new TestCase(name, className, timeSecs, error, failure);

		TestCase[] testCases = new TestCase[] { testCase };
		SurefireTestSuite testSuite =
				new SurefireTestSuite(name, timeSecs, numTests, numErrors, numFailures, testCases);
		assertEquals(name, testSuite.getName());
		assertEquals(timeSecs, testSuite.getTimeSeconds(), 0);
		assertEquals(numTests, testSuite.getNumTests());
		assertEquals(numErrors, testSuite.getNumErrors());
		assertEquals(numFailures, testSuite.getNumFailures());
		assertArrayEquals(testCases, testSuite.getTestcases());

		assertEquals(name, testCase.getName());
		assertEquals(className, testCase.getClassName());
		assertEquals(timeSecs, testCase.getTimeSeconds(), 0);
		assertEquals(error, testCase.getError());
		assertEquals(failure, testCase.getFailure());

		assertEquals(message, error.getMessage());
		assertEquals(type, error.getType());
		assertEquals(body1, error.getBody());
	}
}
