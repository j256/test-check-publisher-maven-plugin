package com.j256.testcheckpublisher.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

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
import com.j256.testcheckpublisher.plugin.frameworks.FrameworkCheckGenerator;
import com.j256.testcheckpublisher.plugin.frameworks.FrameworkCheckGeneratorFactory;
import com.j256.testcheckpublisher.plugin.frameworks.FrameworkTestResults;
import com.j256.testcheckpublisher.plugin.frameworks.FrameworkTestResults.TestFileResult;
import com.j256.testcheckpublisher.plugin.gitcontext.GitContextFinder.GitContext;
import com.j256.testcheckpublisher.plugin.gitcontext.GitContextFinderType;

/**
 * Maven plugin that posts the check results to the server.
 * 
 * @author graywatson
 */
@Mojo(name = "publish", requiresOnline = true)
public class TestCheckPubMojo extends AbstractMojo {

	private static final String DEFAULT_SERVER_URL = "https://testcheckpublisher.256stuff.com/results";
	public static final String DEFAULT_SECRET_ENV_NAME = "TEST_CHECK_PUBLISHER_SECRET";
	private static final int MAX_NUM_RESULTS = 50;
	private static final String MAX_NUM_RESULTS_STR = "50";
	/** ultimate limit to the number of check results we post */
	private static final int ULTIMATE_MAX_NUM_RESULTS = 500;
	private static final String DEFAULT_SECRET_VALUE =
			"This setting should probably not be used for security reasons.  Use the secretEnvName instead.";

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
	private FrameworkCheckGeneratorFactory framework;
	@Parameter
	private GitContextFinderType context;
	@Parameter
	private File testReportDir;
	@Parameter
	private File sourceDir;
	/** verbose log output when mvn -X is used */
	@Parameter
	private boolean verbose;
	/**
	 * Format of the results on github. Not used currently.
	 */
	@Parameter
	private String format;

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
			framework = FrameworkCheckGeneratorFactory.SUREFIRE;
		}

		Log log = getLog();
		log.info("Posting test-check-publisher plugin results to server...");
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

	public void setFramework(FrameworkCheckGeneratorFactory framework) {
		this.framework = framework;
	}

	public void setContext(GitContextFinderType context) {
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

		GitContextFinderType contextFinderType = context;
		if (contextFinderType == null) {
			for (GitContextFinderType type : GitContextFinderType.values()) {
				if (type.isRunning()) {
					contextFinderType = type;
					break;
				}
			}
		}
		GitContext gitContext = findGitContext(contextFinderType, log);
		if (gitContext == null) {
			log.error("Unable to determine git context");
			System.exit(1);
		}

		FrameworkCheckGenerator frameworkGenerator = framework.create(log);

		if (maxNumResults > ULTIMATE_MAX_NUM_RESULTS) {
			maxNumResults = ULTIMATE_MAX_NUM_RESULTS;
		}
		FrameworkTestResults frameworkResults = new FrameworkTestResults();
		log.debug("Loading tests results from framework generator " + framework);
		try {
			frameworkGenerator.loadTestResults(frameworkResults, testReportDir, sourceDir, log);
		} catch (Exception e) {
			log.error("Problems loading test results with framework: " + framework, e);
			System.exit(1);
		}
		if (format != null) {
			// set our format string
			frameworkResults.setFormat(format);
		}
		frameworkResults.limitFileResults(maxNumResults);
		if (verbose) {
			for (TestFileResult result : frameworkResults.getFileResults()) {
				log.debug("result: " + result);
			}
		}

		PublishedTestResults results = new PublishedTestResults(gitContext.getOwner(), gitContext.getRepository(),
				gitContext.getCommitSha(), secret, frameworkResults);

		log.debug("Posting test-check-publisher plugin results to server..." + frameworkResults.asString());
		long before = System.currentTimeMillis();
		postResults(log, frameworkResults, results);
		log.debug("Posting took " + (System.currentTimeMillis() - before) + " ms");
	}

	private GitContext findGitContext(GitContextFinderType finderType, Log log) {
		GitContext gitContext = finderType.findContext(log);
		if (gitContext != null) {
			log.debug("Git context finder " + finderType + ": " + gitContext);
			return gitContext;
		}
		GitContextFinderType defaultType = GitContextFinderType.getDefault();
		if (finderType == defaultType) {
			return null;
		}
		gitContext = defaultType.findContext(log);
		if (gitContext == null) {
			return null;
		} else {
			log.debug("Git context finder " + defaultType + ": " + gitContext);
			return gitContext;
		}
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
				log.info("Posted test-check-publisher plugin results to server: " + frameworkResults.asString());
			} else {
				log.error("Posting test-check-publisher plugin results has failed: " + response.getStatusLine());
				// log the resulting message from the server
				while (true) {
					String line = reader.readLine();
					if (line == null) {
						break;
					} else if (line.length() > 0) {
						log.error("Server response: " + line);
					}
				}
				log.error("Test results were: " + frameworkResults.asString());
			}
		}
	}
}
