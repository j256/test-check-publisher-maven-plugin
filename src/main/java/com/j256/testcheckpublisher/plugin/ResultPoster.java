package com.j256.testcheckpublisher.plugin;

import java.io.IOException;

/**
 * Class that posts results to the server. This is here for testing purposes.
 * 
 * @author graywatson
 */
public interface ResultPoster {

	/**
	 * Post results to the server.
	 */
	public void postResults(PublishedTestResults results) throws IOException;
}
