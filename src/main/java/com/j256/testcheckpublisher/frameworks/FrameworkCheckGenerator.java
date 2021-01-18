package com.j256.testcheckpublisher.frameworks;

/**
 * Framework which processes its metadata and creates a check-run request object.
 * 
 * @author graywatson
 */
public interface FrameworkCheckGenerator {

	/**
	 * Return the request for the framework.
	 */
	public void loadTestResults(FrameworkTestResults testResults) throws Exception;
}
