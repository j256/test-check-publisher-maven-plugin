package com.j256.testcheckpublisher.plugin.frameworks;

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
}
