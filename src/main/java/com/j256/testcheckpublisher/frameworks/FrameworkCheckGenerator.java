package com.j256.testcheckpublisher.frameworks;

import java.util.Collection;

import com.j256.testcheckpublisher.FileInfo;
import com.j256.testcheckpublisher.github.CheckRunRequest.CheckRunOutput;

/**
 * Framework which processes its metadata and creates a check-run request object.
 * 
 * @author graywatson
 */
public interface FrameworkCheckGenerator {

	/**
	 * Return the request for the framework.
	 */
	public CheckRunOutput createRequest(String owner, String repository, String sha, Collection<FileInfo> fileInfos)
			throws Exception;
}
