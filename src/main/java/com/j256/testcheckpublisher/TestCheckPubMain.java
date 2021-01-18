package com.j256.testcheckpublisher;

import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.j256.testcheckpublisher.frameworks.FrameworkTestResults;
import com.j256.testcheckpublisher.frameworks.SurefireFrameworkCheckGenerator;

public class TestCheckPubMain {

	private static final String PUBLISH_URL = "https://testcheckpublisher.256stuff.com/";
	private static final int MAX_NUM_RESULTS = 100;

	public static void main(String[] args) throws Exception {

		// XXX: need to discover the environmental variable names for the owner, repo, sha, etc.
		// could also git config --get remote.origin.url to get owner repo
		// git log -1 gives the sha

		String owner = "j256";
		String repository = "test-check-publisher";
		String commitSha = "217bd75c06c605562b318bdadec91290b11a4f8c";
		String secret = "";

		CloseableHttpClient httpclient = HttpClients.createDefault();
		Gson gson = new GsonBuilder().create();

		SurefireFrameworkCheckGenerator generator = new SurefireFrameworkCheckGenerator();

		FrameworkTestResults frameworkResults = new FrameworkTestResults(MAX_NUM_RESULTS);
		generator.loadTestResults(frameworkResults);

		PublishedTestResults results = new PublishedTestResults(owner, repository, commitSha, secret, frameworkResults);

		HttpPost post = new HttpPost(PUBLISH_URL);
		post.setEntity(new StringEntity(gson.toJson(results)));

		try (CloseableHttpResponse response = httpclient.execute(post);
				Reader reader = new InputStreamReader(response.getEntity().getContent())) {
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				System.out.println("Checks posted.");
			} else {
				System.err.println("ERROR: Check posting failed.");
				// dump the result message to stderr
				char[] buf = new char[1024];
				while (true) {
					int num = reader.read(buf);
					if (num < 0) {
						break;
					} else {
						System.err.print(new String(buf, 0, num));
					}
				}
			}
		}
	}
}
