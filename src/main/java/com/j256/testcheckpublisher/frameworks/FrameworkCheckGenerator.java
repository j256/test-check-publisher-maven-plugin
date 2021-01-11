package com.j256.testcheckpublisher.frameworks;

import com.j256.testcheckpublisher.github.CheckRunRequest.CheckRunOutput;
import com.j256.testcheckpublisher.github.TreeFile;

/**
 * Framework which processes its metadata and creates a check-run request object.
 * 
 * @author graywatson
 */
public interface FrameworkCheckGenerator {

	/**
	 * Return the request for the framework.
	 */
	public CheckRunOutput createRequest(String sha, TreeFile[] treeFiles) throws Exception;
}
