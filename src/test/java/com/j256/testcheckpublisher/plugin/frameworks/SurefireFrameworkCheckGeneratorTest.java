package com.j256.testcheckpublisher.plugin.frameworks;

import java.io.File;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;

public class SurefireFrameworkCheckGeneratorTest {

	@Test
	public void testStuff() {
		FrameworkTestResults results = new FrameworkTestResults();
		SurefireFrameworkCheckGenerator generator = new SurefireFrameworkCheckGenerator();
		generator.loadTestResults(results, null, null, new SystemStreamLog());
		results.limitFileResults(100);
	}

	@Test
	public void testBlankDir() {
		FrameworkTestResults results = new FrameworkTestResults();
		SurefireFrameworkCheckGenerator generator = new SurefireFrameworkCheckGenerator();
		File testReportDir = new File("target/" + getClass().getSimpleName() + ".t");
		testReportDir.deleteOnExit();
		testReportDir.delete();
		testReportDir.mkdir();
		generator.loadTestResults(results, testReportDir, null, new SystemStreamLog());
	}

	@Test
	public void testBlankSourceDir() {
		SurefireFrameworkCheckGenerator generator = new SurefireFrameworkCheckGenerator();
		FrameworkTestResults results = new FrameworkTestResults();
		generator.loadTestResults(results, null, new File("wrong-dir-name"), new SystemStreamLog());
	}

	@Test
	public void testCoverage() {
		SurefireFrameworkCheckGenerator generator = new SurefireFrameworkCheckGenerator();
		generator.loadTestResults(null, new File("wrong-dir-name"), null, new SystemStreamLog());
	}
}
