package com.j256.testcheckpublisher;

import java.io.BufferedReader;
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
import com.j256.testcheckpublisher.frameworks.FrameworkTestResults;
import com.j256.testcheckpublisher.frameworks.SurefireFrameworkCheckGenerator;
import com.j256.testcheckpublisher.gitcontext.CircleCiGitContextFinder;
import com.j256.testcheckpublisher.gitcontext.CommandLineGitContextFinder;
import com.j256.testcheckpublisher.gitcontext.GitContextFinder.GitContext;

/**
 * Maven plugin that posts the check results to the server.
 * 
 * @author graywatson
 */
@Mojo(name = "publish")
public class TestCheckPubMojo extends AbstractMojo {

	@Parameter(property = "server.url", defaultValue = "https://testcheckpublisher.256stuff.com/")
	private String publishUrl;
	@Parameter(property = "max.num.results", defaultValue = "100")
	private int maxNumResults;
	@Parameter(property = "secret.env.name", defaultValue = "TEST_CHECK_PUBLISHER_SECRET")
	private String secretEnvName;
	@Parameter(property = "generator", defaultValue = "SUREFIRE")
	private Generator generator;
	@Parameter(property = "context", defaultValue = "GIT_COMMAND")
	private Context context;

	@Override
	public void execute() throws MojoExecutionException {
		Log log = getLog();
		log.info("Hello, world.");
		try {
			publish(log);
		} catch (IOException ioe) {
			throw new MojoExecutionException("IOException trying to publish our test checks to github", ioe);
		}
	}

	private void publish(Log log) throws IOException {

		String secret = System.getenv(secretEnvName);
		if (secret == null) {
			log.error("Could not find required env variable: " + secretEnvName);
			System.exit(1);
		}

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

		CloseableHttpClient httpclient = HttpClients.createDefault();
		Gson gson = new GsonBuilder().create();

		SurefireFrameworkCheckGenerator frameworkGenerator;
		switch (generator) {
			case SUREFIRE:
			default:
				frameworkGenerator = new SurefireFrameworkCheckGenerator();
				break;
		}

		FrameworkTestResults frameworkResults = new FrameworkTestResults(maxNumResults);
		frameworkGenerator.loadTestResults(frameworkResults);

		PublishedTestResults results = new PublishedTestResults(gitContext.getOwner(), gitContext.getRepository(),
				gitContext.getCommitSha(), secret, frameworkResults);

		HttpPost post = new HttpPost(publishUrl);
		post.setEntity(new StringEntity(gson.toJson(results)));

		try (CloseableHttpResponse response = httpclient.execute(post);
				BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				log.info("Checks posted.");
			} else {
				log.error("Check posting failed.");
				// dump the result message to stderr
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
			}
		}
	}

	/**
	 * Types of generators that we support.
	 */
	public static enum Generator {
		SUREFIRE,
		// end
		;
	}

	/**
	 * Types of generators that we support.
	 */
	public static enum Context {
		CIRCLE_CI,
		GIT_COMMAND,
		// end
		;
	}
}
