package com.j256.testcheckpublisher.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.maven.plugin.logging.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Posts our results to the server
 * 
 * @author graywatson
 */
public class ServerResultPoster implements ResultPoster {

	private final String serverUrl;
	private final Log log;

	public ServerResultPoster(String serverUrl, Log log) {
		this.serverUrl = serverUrl;
		this.log = log;
	}

	@Override
	public void postResults(PublishedTestResults results) throws IOException {

		CloseableHttpClient httpclient = HttpClients.createDefault();
		Gson gson = new GsonBuilder().create();

		HttpPost post = new HttpPost(serverUrl);
		post.setEntity(new StringEntity(gson.toJson(results)));

		try (CloseableHttpResponse response = httpclient.execute(post);
				BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				log.info("Posted test-check-publisher plugin results to server: " + results.asString());
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
				log.error("Test results were: " + results.asString());
			}
		}
	}
}
