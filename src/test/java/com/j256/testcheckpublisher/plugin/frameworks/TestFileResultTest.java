package com.j256.testcheckpublisher.plugin.frameworks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import com.j256.testcheckpublisher.plugin.frameworks.TestFileResult.TestLevel;

public class TestFileResultTest {

	@Test
	public void testStuff() {
		String path = "path";
		int startLineNumber = 1;
		int endLineNumber = 2;
		TestLevel level = TestLevel.NOTICE;
		float timeSecs = 0.1F;
		String testName = "test-name";
		String message = "message";
		String details = "details";
		TestFileResult result1 =
				new TestFileResult(path, startLineNumber, endLineNumber, level, timeSecs, testName, message, details);
		assertEquals(path, result1.getPath());
		assertEquals(startLineNumber, result1.getStartLineNumber());
		assertEquals(endLineNumber, result1.getEndLineNumber());
		assertEquals(level, result1.getTestLevel());
		assertEquals(timeSecs, result1.getTimeSeconds(), 0);
		assertEquals(testName, result1.getTestName());
		assertEquals(message, result1.getMessage());
		assertEquals(details, result1.getDetails());

		TestFileResult result2 =
				new TestFileResult(path, startLineNumber, endLineNumber, level, timeSecs, testName, message, details);
		assertEquals(result1, result2);
		assertEquals(result1.toString(), result2.toString());
		assertEquals(result2, result1);
		assertEquals(0, result1.compareTo(result2));
		assertEquals(result1.hashCode(), result2.hashCode());

		result2 = new TestFileResult(null, startLineNumber, endLineNumber, level, timeSecs, testName, message, details);
		testNotEquals(result1, result2);
		assertEquals(1, result1.compareTo(result2));
		assertEquals(-1, result2.compareTo(result1));

		result2 = new TestFileResult(path, 2, endLineNumber, level, timeSecs, testName, message, details);
		testNotEquals(result1, result2);
		assertEquals(0, result1.compareTo(result2));
		assertEquals(0, result2.compareTo(result1));

		result2 = new TestFileResult(path, startLineNumber, 4, TestLevel.FAILURE, timeSecs, testName, message, details);
		testNotEquals(result1, result2);
		assertEquals(1, result1.compareTo(result2));
		assertEquals(-1, result2.compareTo(result1));

		result2 = new TestFileResult(path, startLineNumber, endLineNumber, null, timeSecs, testName, message, details);
		testNotEquals(result1, result2);
		assertEquals(-1, result1.compareTo(result2));
		assertEquals(1, result2.compareTo(result1));

		result2 = new TestFileResult(path, startLineNumber, endLineNumber, level, 0.2F, testName, message, details);
		testNotEquals(result1, result2);
		assertEquals(0, result1.compareTo(result2));
		assertEquals(0, result2.compareTo(result1));

		result2 = new TestFileResult(path, startLineNumber, endLineNumber, level, timeSecs, null, message, details);
		testNotEquals(result1, result2);
		assertEquals(0, result1.compareTo(result2));
		assertEquals(0, result2.compareTo(result1));

		result2 = new TestFileResult(path, startLineNumber, endLineNumber, level, timeSecs, testName, null, details);
		testNotEquals(result1, result2);
		assertEquals(0, result1.compareTo(result2));
		assertEquals(0, result2.compareTo(result1));

		result2 = new TestFileResult(path, startLineNumber, endLineNumber, level, timeSecs, testName, message, null);
		testNotEquals(result1, result2);
		assertEquals(0, result1.compareTo(result2));
		assertEquals(0, result2.compareTo(result1));
	}

	@Test
	public void testCoverage() {
		TestFileResult result1 =
				new TestFileResult("path", 1, 2, TestLevel.NOTICE, 0.0F, "test-name", "message", "details");
		assertFalse(result1.equals(null));
		assertFalse(result1.equals(new Object()));
		assertEquals("Notice", TestLevel.NOTICE.getPrettyString());
	}

	private void testNotEquals(TestFileResult result1, TestFileResult result2) {
		assertEquals(result1, result1);
		assertEquals(result2, result2);
		assertNotEquals(result1, result2);
		assertNotEquals(result2, result1);
		assertNotEquals(result1.hashCode(), result2.hashCode());
		assertEquals(0, result1.compareTo(result1));
		assertEquals(0, result2.compareTo(result2));
	}
}
