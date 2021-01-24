package com.j256.testcheckpublisher.plugin.frameworks;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;

public class SurefireFrameworkCheckGeneratorTest {

	@Test
	public void testStuff() {
		SurefireFrameworkCheckGenerator generator = new SurefireFrameworkCheckGenerator();
		FrameworkTestResults results = new FrameworkTestResults(100);
		generator.loadTestResults(results, null, null, new SystemStreamLog());
	}
}
