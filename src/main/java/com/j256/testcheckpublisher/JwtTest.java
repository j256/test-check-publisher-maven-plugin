package com.j256.testcheckpublisher;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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

	private static final String TREE_TYPE = "tree";

	public static void main(String[] args) throws Exception {

		PrivateKey key = KeyHandling.loadKey(args[0]);

		// app-id
		String issuer = "94919";
		long ttlMillis = 10 * 60 * 1000;

		// The JWT signature algorithm we will be using to sign the token

		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.RS256;

		String bearerToken = buildBearerToken(key, issuer, ttlMillis, signatureAlgorithm);

		String owner = "j256";
		String repository = "test-check-publisher";

		CloseableHttpClient httpclient = HttpClients.createDefault();
		Gson gson = new GsonBuilder().create();

		int installationId = findInstallationId(httpclient, gson, bearerToken, owner);
		if (installationId < 0) {
			System.err.println("Account not found in installations: " + owner);
			System.exit(1);
		}

		String sha = "9cdb380d857d8498ea8ebead6639b1d5c77c5416";

		String accessToken = createAccessToken(httpclient, gson, bearerToken, repository, installationId);

		Collection<FileInfo> fileInfos = getCommitInfo(httpclient, gson, accessToken, owner, repository, sha);

		SurefireFrameworkCheckGenerator generator = new SurefireFrameworkCheckGenerator();

		CheckRunOutput output = generator.createRequest(sha, fileInfos);
		CheckRunRequest request = new CheckRunRequest("Surefile unit test results", sha, output);

		String path = "/repos/" + owner + "/" + repository + "/check-runs";

		HttpPost post = new HttpPost("https://api.github.com" + path);
		post.addHeader("Authorization", "token " + accessToken);
		post.addHeader("Accept", "application/vnd.github.v3+json");

		post.setEntity(new StringEntity(gson.toJson(request)));

		CloseableHttpResponse response = httpclient.execute(post);

		// Builds the JWT and serializes it to a compact, URL-safe string
		System.out.println(IoUtils.inputStreamToString(response.getEntity().getContent()));
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

	private static Collection<FileInfo> getCommitInfo(CloseableHttpClient httpclient, Gson gson, String accessToken,
			String owner, String repository, String topSha)
			throws JsonSyntaxException, UnsupportedOperationException, IOException {

		String path = "/repos/" + owner + "/" + repository + "/git/commits/" + topSha;
		HttpGet get = new HttpGet("https://api.github.com" + path);
		// get.addHeader("Authorization", "Bearer " + bearerToken);
		get.addHeader("Accept", "application/vnd.github.v3+json");

		CommitInfoResponse commitInfoResponse;
		try (CloseableHttpResponse response = httpclient.execute(get);
				Reader contentReader = new InputStreamReader(response.getEntity().getContent());) {
			String str = IoUtils.readerToString(contentReader);
			commitInfoResponse = gson.fromJson(str, CommitInfoResponse.class);
			// commitInfoResponse = gson.fromJson(contentReader, CommitInfoResponse.class);
		}

		List<FileInfo> fileInfos = new ArrayList<>();
		Queue<ShaPathPrefix> shaQueue = new LinkedList<>();
		shaQueue.add(new ShaPathPrefix(commitInfoResponse.getTree().getSha(), ""));

		while (true) {
			ShaPathPrefix shaPath = shaQueue.poll();
			if (shaPath == null) {
				return fileInfos;
			}
			// GET /repos/{owner}/{repo}/git/trees/{tree_sha}
			path = "/repos/" + owner + "/" + repository + "/git/trees/" + shaPath.sha;

			get = new HttpGet("https://api.github.com" + path);
			get.addHeader("Authorization", "token " + accessToken);
			get.addHeader("Accept", "application/vnd.github.v3+json");

			try (CloseableHttpResponse response = httpclient.execute(get)) {
				String str = IoUtils.inputStreamToString(response.getEntity().getContent());
				// TreeInfoResponse treeInfoResponse =
				// gson.fromJson(new InputStreamReader(response.getEntity().getContent()), TreeInfoResponse.class);
				TreeInfoResponse treeInfoResponse = gson.fromJson(str, TreeInfoResponse.class);
				if (treeInfoResponse.getTreeFiles() != null) {
					for (TreeFile treeFile : treeInfoResponse.getTreeFiles()) {
						if (TREE_TYPE.equals(treeFile.getType())) {
							shaQueue.add(new ShaPathPrefix(treeFile.getSha(),
									shaPath.pathPrefix + treeFile.getPath() + "/"));
						} else {
							// make our path a relative path from root
							fileInfos.add(new FileInfo(shaPath.pathPrefix + treeFile.getPath(), treeFile.getPath(),
									treeFile.getSha()));
						}
					}
				}
			}
		}
	}

	private static class ShaPathPrefix {
		final String sha;
		final String pathPrefix;

		public ShaPathPrefix(String sha, String pathPrefix) {
			this.sha = sha;
			this.pathPrefix = pathPrefix;
		}
	}
}
