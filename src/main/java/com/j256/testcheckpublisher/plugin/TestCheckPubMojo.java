package com.j256.testcheckpublisher.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.j256.testcheckpublisher.plugin.frameworks.FrameworkTestResults;
import com.j256.testcheckpublisher.plugin.frameworks.SurefireFrameworkCheckGenerator;
import com.j256.testcheckpublisher.plugin.gitcontext.CircleCiGitContextFinder;
import com.j256.testcheckpublisher.plugin.gitcontext.CommandLineGitContextFinder;
import com.j256.testcheckpublisher.plugin.gitcontext.GitContextFinder.GitContext;

/**
 * Maven plugin that posts the check results to the server.
 * 
 * @author graywatson
 */
@Mojo(name = "publish", requiresOnline = true)
public class TestCheckPubMojo extends AbstractMojo {

	private static final String DEFAULT_SERVER_URL = "https://testcheckpublisher.256stuff.com/";
	public static final String DEFAULT_SECRET_ENV_NAME = "TEST_CHECK_PUBLISHER_SECRET";
	public static final int MAX_NUM_RESULTS = 50;
	public static final String MAX_NUM_RESULTS_STR = "50";
	public static final int ULTIMATE_MAX_NUM_RESULTS = 500;
	public static final String DEFAULT_SECRET_VALUE =
			"None.  This should probably not be used for security reasons.  Use the secretEnvName instead.";

	@Parameter(defaultValue = DEFAULT_SERVER_URL)
	private String serverUrl;
	@Parameter(defaultValue = MAX_NUM_RESULTS_STR)
	private int maxNumResults = -1;
	@Parameter(defaultValue = DEFAULT_SECRET_ENV_NAME)
	private String secretEnvName;
	/**
	 * This is not recommended to use for security reasons so that your secret is not checked in or otherwise exposed.
	 * Use the {@link #secretEnvName} and set the secret in an environment variable.
	 */
	@Parameter(defaultValue = DEFAULT_SECRET_VALUE)
	private String secretValue;
	@Parameter(defaultValue = "SUREFIRE")
	private Framework framework;
	@Parameter(defaultValue = "GIT_COMMAND")
	private ContextFinder context;
	@Parameter
	private File testReportDir;
	@Parameter
	private File sourceDir;

	@Override
	public void execute() throws MojoExecutionException {

		if (secretEnvName == null) {
			secretEnvName = DEFAULT_SECRET_ENV_NAME;
		}
		if (serverUrl == null) {
			serverUrl = DEFAULT_SERVER_URL;
		}
		if (maxNumResults < 0) {
			maxNumResults = MAX_NUM_RESULTS;
		}
		if (framework == null) {
			framework = Framework.SUREFIRE;
		}
		if (context == null) {
			context = ContextFinder.GIT_COMMAND;
		}

		Log log = getLog();
		log.info("Publishing test information to server...");
		try {
			publish(log);
		} catch (IOException ioe) {
			throw new MojoExecutionException("IOException trying to publish our test checks to github", ioe);
		}
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public void setMaxNumResults(int maxNumResults) {
		this.maxNumResults = maxNumResults;
	}

	public void setSecretEnvName(String secretEnvName) {
		this.secretEnvName = secretEnvName;
	}

	public void setSecretValue(String secretValue) {
		this.secretValue = secretValue;
	}

	public void setFramework(Framework framework) {
		this.framework = framework;
	}

	public void setContext(ContextFinder context) {
		this.context = context;
	}

	public void setTestReportDir(File testReportDir) {
		this.testReportDir = testReportDir;
	}

	public void setSourceDir(File sourceDir) {
		this.sourceDir = sourceDir;
	}

	private void publish(Log log) throws IOException {

		// look for our secret environ variable
		String secret = secretValue;
		if (secret == null || secret.equals(DEFAULT_SECRET_VALUE)) {
			secret = System.getenv(secretEnvName);
			if (secret == null) {
				log.error("Could not find required env variable: " + secretEnvName);
				System.exit(1);
			}
		}

		// find the git-context of the local directory structure
		GitContext gitContext;
		switch (context) {
			case CIRCLE_CI:
				gitContext = new CircleCiGitContextFinder().findContext();
				break;
			case GIT_COMMAND:
			default:
				gitContext = new CommandLineGitContextFinder().findContext();
				break;
		}
		if (gitContext == null) {
			log.error("Could not determine git state using context: " + context);
			System.exit(1);
		}
		log.debug("Git context finder " + context + ": " + gitContext);

		SurefireFrameworkCheckGenerator frameworkGenerator;
		switch (framework) {
			case SUREFIRE:
			default:
				frameworkGenerator = new SurefireFrameworkCheckGenerator();
				break;
		}

		if (maxNumResults > ULTIMATE_MAX_NUM_RESULTS) {
			maxNumResults = ULTIMATE_MAX_NUM_RESULTS;
		}
		FrameworkTestResults frameworkResults = new FrameworkTestResults(maxNumResults);
		log.debug("Loading tests results from framework generator " + framework);
		frameworkGenerator.loadTestResults(frameworkResults, testReportDir, sourceDir, log);

		PublishedTestResults results = new PublishedTestResults(gitContext.getOwner(), gitContext.getRepository(),
				gitContext.getCommitSha(), secret, frameworkResults);

		log.debug("Posting results to server...");
		postResults(log, frameworkResults, results);
	}

	private void postResults(Log log, FrameworkTestResults frameworkResults, PublishedTestResults results)
			throws IOException {

		CloseableHttpClient httpclient = HttpClients.createDefault();
		Gson gson = new GsonBuilder().create();

		HttpPost post = new HttpPost(serverUrl);
		post.setEntity(new StringEntity(gson.toJson(results)));

		try (CloseableHttpResponse response = httpclient.execute(post);
				BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				log.info("Checks have been posted: " + frameworkResults.asString());
			} else {
				log.error("Check posting failed: " + response.getStatusLine());
				// log the resulting message from the server
				StringWriter writer = new StringWriter();
				while (true) {
					String line = reader.readLine();
					if (line == null) {
						break;
					} else if (line.length() > 0) {
						log.error("Server response: " + line);
					}
				}
				log.error(writer.toString());
				log.error("Test results were: " + frameworkResults.asString());
			}
		}
	}

	/**
	 * Types of generators that we support.
	 */
	public static enum Framework {
		SUREFIRE,
		// end
		;
	}

	/**
	 * Types of generators that we support.
	 */
	public static enum ContextFinder {
		CIRCLE_CI,
		GIT_COMMAND,
		// end
		;
	}
}
