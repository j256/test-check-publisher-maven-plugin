package com.j256.testcheckpublisher.plugin.frameworks;

import java.io.File;

import org.apache.maven.plugin.logging.Log;

/**
 * Framework which processes its metadata and creates a check-run request object.
 * 
 * @author graywatson
 */
public interface FrameworkCheckGenerator {

	/**
	 * Populate the testResults class with information from this framework.
	 */
	public void loadTestResults(FrameworkTestResults testResults, File testReportDir, File sourceDir, Log log)
			throws Exception;
}
