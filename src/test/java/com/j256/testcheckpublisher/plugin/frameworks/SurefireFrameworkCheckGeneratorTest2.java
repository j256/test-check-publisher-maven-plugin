package com.j256.testcheckpublisher.plugin.frameworks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;

public class SurefireFrameworkCheckGeneratorTest2 {

	@Test
	public void testStuff() {
		SurefireFrameworkCheckGenerator generator = new SurefireFrameworkCheckGenerator();
		FrameworkTestResults results = new FrameworkTestResults();
		generator.loadTestResults(results, new File("target/surefire-reports"), new File("."), new SystemStreamLog());
	}

	@Test
	public void testCoverage() throws IOException {
		SurefireFrameworkCheckGenerator generator = new SurefireFrameworkCheckGenerator();
		FrameworkTestResults results = new FrameworkTestResults();
		generator.loadTestResults(results, new File("bad-dir"), new File("."), new SystemStreamLog());
		File testDir = new File("target/some-random-dir");
		testDir.mkdirs();
		generator.loadTestResults(results, testDir, new File("bad-dir"), new SystemStreamLog());
		testDir = new File("target/surefire-reports");
		generator.loadTestResults(results, testDir, new File("bad-dir"), new SystemStreamLog());

		File testFile = new File(testDir, "TEST-something.xml");
		FileWriter writer = new FileWriter(testFile);
		writer.append("not xml");
		writer.close();
		generator.loadTestResults(results, testDir, null, new SystemStreamLog());
	}
}
