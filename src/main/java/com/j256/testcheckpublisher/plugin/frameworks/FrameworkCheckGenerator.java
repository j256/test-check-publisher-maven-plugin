package com.j256.testcheckpublisher.plugin.frameworks;

import java.io.File;

/**
 * Framework which processes its metadata and creates a check-run request object.
 * 
 * @author graywatson
 */
public interface FrameworkCheckGenerator {

	/**
	 * Return the request for the framework.
	 */
	public void loadTestResults(FrameworkTestResults testResults, File testWorkingDir) throws Exception;
}
