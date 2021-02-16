package com.j256.testcheckpublisher.plugin.frameworks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;

public class SurefireFrameworkCheckGeneratorTest {

	@Test
	public void testStuff() {
		FrameworkTestResults results = new FrameworkTestResults();
		SurefireFrameworkCheckGenerator generator = new SurefireFrameworkCheckGenerator();
		generator.loadTestResults(results, null, null, new SystemStreamLog());
		results.limitFileResults(100, false);
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
	public void testCoverage() throws IOException {
		SurefireFrameworkCheckGenerator generator = new SurefireFrameworkCheckGenerator();
		generator.loadTestResults(null, new File("wrong-dir-name"), null, new SystemStreamLog());

		FrameworkTestResults results = new FrameworkTestResults();
		generator.loadTestResults(results, new File("bad-test-dir"), new File("."), new SystemStreamLog());
		File testDir = new File("target/some-random-dir");
		testDir.mkdirs();
		for (File file : testDir.listFiles()) {
			file.delete();
		}
		// empty testdir and bad source-dir
		generator.loadTestResults(results, testDir, new File("bad-source-dir"), new SystemStreamLog());
		// file instead of a directory
		generator.loadTestResults(results, testDir, new File("/dev/null"), new SystemStreamLog());

		File testFile = new File(testDir, "TEST-something.xml");
		FileWriter writer = new FileWriter(testFile);
		writer.append("not xml");
		writer.close();
		generator.loadTestResults(results, testDir, null, new SystemStreamLog());
	}

}
