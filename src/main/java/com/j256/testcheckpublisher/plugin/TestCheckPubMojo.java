package com.j256.testcheckpublisher.plugin;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.j256.testcheckpublisher.plugin.frameworks.FrameworkCheckGenerator;
import com.j256.testcheckpublisher.plugin.frameworks.FrameworkCheckGeneratorFactory;
import com.j256.testcheckpublisher.plugin.frameworks.FrameworkTestResults;
import com.j256.testcheckpublisher.plugin.frameworks.TestFileResult;
import com.j256.testcheckpublisher.plugin.gitcontext.GitContextFinder.GitContext;
import com.j256.testcheckpublisher.plugin.gitcontext.GitContextFinderType;

/**
 * Maven plugin that posts the check results to the server for annotating a commit with the checks-api.
 * 
 * @author graywatson
 */
@Mojo(name = "publish", requiresOnline = true)
public class TestCheckPubMojo extends AbstractMojo {

	private static final String DEFAULT_SERVER_URL = "https://testcheckpublisher.256stuff.com/results";
	public static final String DEFAULT_SECRET_ENV_NAME = "TEST_CHECK_PUBLISHER_SECRET";
	private static final int DEFAULT_MAX_NUM_RESULTS = 50;
	private static final String MAX_NUM_RESULTS_STR = "50";
	/** ultimate limit to the number of check results we post */
	private static final int ULTIMATE_MAX_NUM_RESULTS = 500;
	private static final String DEFAULT_SECRET_VALUE =
			"This setting should probably not be used for security reasons.  Use the secretEnvName instead.";

	/** URL of the server in case you want to run your own. */
	@Parameter(defaultValue = DEFAULT_SERVER_URL)
	private String serverUrl = DEFAULT_SERVER_URL;
	/** Maximum number of results to post to the server up to 500. */
	@Parameter(defaultValue = MAX_NUM_RESULTS_STR)
	private int maxNumResults = DEFAULT_MAX_NUM_RESULTS;
	/** Name of the environmental variable that holds the secret value we need */
	@Parameter(defaultValue = DEFAULT_SECRET_ENV_NAME)
	private String secretEnvName = DEFAULT_SECRET_ENV_NAME;
	/**
	 * This is not recommended to use for security reasons so that your secret is not checked in or otherwise exposed.
	 * Use the {@link #secretEnvName} and set the secret in an environment variable.
	 */
	@Parameter(defaultValue = DEFAULT_SECRET_VALUE)
	private String secretValue;
	/** Test framework that reads in the test results and builds our json entity to post. */
	@Parameter(defaultValue = "SUREFIRE")
	private FrameworkCheckGeneratorFactory framework = FrameworkCheckGeneratorFactory.SUREFIRE;
	/** How we find the git information including owner, repo, and commit-sha. */
	@Parameter
	private GitContextFinderType context;
	/** Location of the test-reports read by the framework. The framework supplies the default. */
	@Parameter
	private File testReportDir;
	/** Location of the sources for locating files in the repo. */
	@Parameter(defaultValue = ".")
	private File sourceDir;
	/** Increases the log output when mvn -X is used */
	@Parameter
	private boolean verbose;
	/** Comma separated list of tokens that affect the look of the results on github. */
	@Parameter
	private String format;
	/** Ignore posting any information about tests that pass. */
	@Parameter
	private boolean ignorePass;

	private ResultPoster resultPoster;
	private boolean throwOnError;

	@Override
	public void execute() throws MojoExecutionException {

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

	/**
	 * For testing.
	 */
	void setResultPoster(ResultPoster resultPoster) {
		this.resultPoster = resultPoster;
	}

	/**
	 * For testing.
	 */
	void setThrowOnError(boolean throwOnError) {
		this.throwOnError = throwOnError;
	}

	private void publish(Log log) throws IOException {

		// look for our secret environ variable
		String secret = secretValue;
		if (secret == null || secret.equals(DEFAULT_SECRET_VALUE)) {
			secret = System.getenv(secretEnvName);
			if (secret == null) {
				log.error("Could not find required env variable: " + secretEnvName);
				throwOrExit();
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
			throwOrExit();
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
			throwOrExit();
		}
		frameworkResults.limitFileResults(maxNumResults, ignorePass);
		if (verbose) {
			for (TestFileResult result : frameworkResults.getFileResults()) {
				log.debug("result: " + result);
			}
		}

		PublishedTestResults results = new PublishedTestResults(gitContext.getOwner(), gitContext.getRepository(),
				gitContext.getCommitSha(), secret, format, frameworkResults);

		log.debug("Posting test-check-publisher plugin results to server..." + frameworkResults.asString());
		long before = System.currentTimeMillis();
		if (resultPoster == null) {
			resultPoster = new ServerResultPoster(serverUrl, log);
		}
		resultPoster.postResults(results);
		log.debug("Posting took " + (System.currentTimeMillis() - before) + " ms");
	}

	private GitContext findGitContext(GitContextFinderType finderType, Log log) {
		GitContext gitContext = finderType.findContext(log);
		if (gitContext != null) {
			return gitContext;
		}
		// error already logged
		GitContextFinderType defaultType = GitContextFinderType.getDefault();
		if (finderType == defaultType) {
			return null;
		}
		gitContext = defaultType.findContext(log);
		// might return null, error already logged
		return gitContext;
	}

	private void throwOrExit() {
		if (throwOnError) {
			throw new IllegalStateException("Got error during testing.  See error logs.");
		} else {
			System.exit(1);
		}
	}
}
