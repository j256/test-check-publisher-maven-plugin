package com.j256.testcheckpublisher;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.security.PrivateKey;
import java.util.Date;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.j256.testcheckpublisher.frameworks.SurefireFrameworkCheckGenerator;
import com.j256.testcheckpublisher.github.AccessTokenRequest;
import com.j256.testcheckpublisher.github.AccessTokensResponse;
import com.j256.testcheckpublisher.github.CheckRunRequest;
import com.j256.testcheckpublisher.github.CheckRunRequest.CheckRunOutput;
import com.j256.testcheckpublisher.github.CommitInfoResponse;
import com.j256.testcheckpublisher.github.InstallationInfo;
import com.j256.testcheckpublisher.github.TreeFile;
import com.j256.testcheckpublisher.github.TreeInfoResponse;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtTest {

	public static void main(String[] args) throws Exception {

		PrivateKey key = KeyHandling
				.loadKey("/Users/graywatson/Downloads/unit-test-checks-maven-plugin.2021-01-10.private-key.pem");

		// app-id
		String issuer = "94919";
		long ttlMillis = 10 * 60 * 1000;

		// The JWT signature algorithm we will be using to sign the token

		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.RS256;

		String bearerToken = buildBearerToken(key, issuer, ttlMillis, signatureAlgorithm);

		String owner = "j256";
		String repository = "simplelogging";

		CloseableHttpClient httpclient = HttpClients.createDefault();
		Gson gson = new GsonBuilder().create();

		int installationId = findInstallationId(httpclient, gson, bearerToken, owner);
		if (installationId < 0) {
			System.err.println("Account not found in installations: " + owner);
			System.exit(1);
		}

		String sha = "74a3254f505d7dd652f9a58398ecd20271fbcd54";

		String accessToken = createAccessToken(httpclient, gson, bearerToken, repository, installationId);

		TreeFile[] treeFiles = getCommitInfo(httpclient, gson, accessToken, owner, repository, sha);

		SurefireFrameworkCheckGenerator generator = new SurefireFrameworkCheckGenerator();

		CheckRunOutput output = generator.createRequest(sha, treeFiles);
		CheckRunRequest request = new CheckRunRequest("Surefile unit test results", sha, output);

		String path = "/repos/" + owner + "/" + repository + "/check-runs";

		HttpPost post = new HttpPost("https://api.github.com" + path);
		post.addHeader("Authorization", "token " + accessToken);
		post.addHeader("Accept", "application/vnd.github.v3+json");

		post.setEntity(new StringEntity(gson.toJson(request)));

		CloseableHttpResponse response = httpclient.execute(post);

		// Builds the JWT and serializes it to a compact, URL-safe string
		System.out.println(inputStreamToString(response.getEntity().getContent()));
	}

	private static String buildBearerToken(PrivateKey key, String issuer, long ttlMillis,
			SignatureAlgorithm signatureAlgorithm) {
		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);
		JwtBuilder builder = Jwts.builder()//
				.setIssuedAt(now)
				.setIssuer(issuer)
				.signWith(key, signatureAlgorithm);

		// if it has been specified, let's add the expiration
		if (ttlMillis > 0) {
			long expMillis = nowMillis + ttlMillis;
			Date exp = new Date(expMillis);
			builder.setExpiration(exp);
		}
		return builder.compact();
	}

	private static int findInstallationId(CloseableHttpClient httpclient, Gson gson, String bearerToken, String owner)
			throws ClientProtocolException, IOException {
		HttpGet get = new HttpGet("https://api.github.com/app/installations");
		get.addHeader("Authorization", "Bearer " + bearerToken);
		get.addHeader("Accept", "application/vnd.github.v3+json");
		try (CloseableHttpResponse response = httpclient.execute(get)) {

			InstallationInfo[] installations =
					gson.fromJson(new InputStreamReader(response.getEntity().getContent()), InstallationInfo[].class);
			// System.out.println("Installations: " + Arrays.toString(result));

			for (InstallationInfo installation : installations) {
				if (owner.equals(installation.getAccount().getLogin())) {
					return installation.getId();
				}
			}
			return -1;
		}
	}

	private static String createAccessToken(CloseableHttpClient httpclient, Gson gson, String bearerToken,
			String repository, int installationId)
			throws JsonSyntaxException, UnsupportedOperationException, IOException {
		String path = "/app/installations/" + installationId + "/access_tokens";
		HttpPost post = new HttpPost("https://api.github.com" + path);
		post.addHeader("Authorization", "Bearer " + bearerToken);
		post.addHeader("Accept", "application/vnd.github.v3+json");

		AccessTokenRequest request = new AccessTokenRequest(installationId, new String[] { repository });
		post.setEntity(new StringEntity(gson.toJson(request)));

		try (CloseableHttpResponse response = httpclient.execute(post)) {

			AccessTokensResponse tokens =
					gson.fromJson(new InputStreamReader(response.getEntity().getContent()), AccessTokensResponse.class);
			return tokens.getToken();
		}
	}

	private static TreeFile[] getCommitInfo(CloseableHttpClient httpclient, Gson gson, String accessToken, String owner,
			String repository, String sha) throws JsonSyntaxException, UnsupportedOperationException, IOException {

		String path = "/repos/" + owner + "/" + repository + "/git/commits/" + sha;
		HttpGet get = new HttpGet("https://api.github.com" + path);
		// get.addHeader("Authorization", "Bearer " + bearerToken);
		get.addHeader("Accept", "application/vnd.github.v3+json");

		CommitInfoResponse commitInfoResponse;
		try (CloseableHttpResponse response = httpclient.execute(get);
				Reader contentReader = new InputStreamReader(response.getEntity().getContent());) {
			String str = readerToString(contentReader);
			commitInfoResponse = gson.fromJson(str, CommitInfoResponse.class);
			// commitInfoResponse = gson.fromJson(contentReader, CommitInfoResponse.class);
		}

		// GET /repos/{owner}/{repo}/git/trees/{tree_sha}
		path = "/repos/" + owner + "/" + repository + "/git/trees/" + commitInfoResponse.getTree().getSha();

		get = new HttpGet("https://api.github.com" + path);
		get.addHeader("Authorization", "token " + accessToken);
		get.addHeader("Accept", "application/vnd.github.v3+json");

		try (CloseableHttpResponse response = httpclient.execute(get)) {
			String str = inputStreamToString(response.getEntity().getContent());
			// TreeInfoResponse treeInfoResponse =
			// gson.fromJson(new InputStreamReader(response.getEntity().getContent()), TreeInfoResponse.class);
			TreeInfoResponse treeInfoResponse = gson.fromJson(str, TreeInfoResponse.class);
			return treeInfoResponse.getTreeFiles();
		}
	}

	private static String inputStreamToString(InputStream inputStream)
			throws UnsupportedOperationException, IOException {
		try (Reader reader = new InputStreamReader(inputStream)) {
			return readerToString(reader);
		}
	}

	private static String readerToString(Reader reader) throws UnsupportedOperationException, IOException {
		try (StringWriter writer = new StringWriter()) {
			char[] chars = new char[1024];
			while (true) {
				int len = reader.read(chars);
				if (len < 0) {
					break;
				}
				writer.write(chars, 0, len);
			}
			return writer.toString();
		}
	}
}
