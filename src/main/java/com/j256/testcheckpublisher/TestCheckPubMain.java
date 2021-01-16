package com.j256.testcheckpublisher;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.j256.testcheckpublisher.github.CommitInfoResponse.ChangedFile;
import com.j256.testcheckpublisher.github.IdResponse;
import com.j256.testcheckpublisher.github.InstallationInfo;
import com.j256.testcheckpublisher.github.TreeFile;
import com.j256.testcheckpublisher.github.TreeInfoResponse;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class TestCheckPubMain {

	private static final String TREE_TYPE = "tree";
	/**
	 * XXX: we should limit and not drown the user in check information if a test blows up. Also, there is a limit of 50
	 * checks per request and then we need to do an update.
	 */
	@SuppressWarnings("unused")
	private final int MAX_NUMBER_ANNOTATIONS = 50;

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

		String commitSha = "217bd75c06c605562b318bdadec91290b11a4f8c";

		String accessToken = createAccessToken(httpclient, gson, bearerToken, repository, installationId);

		CommitInfoResponse commitInfo = getCommitInfo(httpclient, gson, accessToken, owner, repository, commitSha);
		Set<String> commitPathSet = new HashSet<>();
		if (commitInfo.getFiles() != null) {
			for (ChangedFile file : commitInfo.getFiles()) {
				// we ignore "removed" files
				if (!"removed".equals(file.getStatus())) {
					commitPathSet.add(file.getFilename());
				}
			}
		}

		Collection<FileInfo> fileInfos =
				getTreeFiles(httpclient, gson, accessToken, owner, repository, commitInfo.getTreeSha());
		for (FileInfo fileInfo : fileInfos) {
			if (commitPathSet.contains(fileInfo.path)) {
				fileInfo.setInCommit(true);
			}
		}

		SurefireFrameworkCheckGenerator generator = new SurefireFrameworkCheckGenerator();

		CheckRunOutput output = generator.createRequest(owner, repository, commitSha, fileInfos);
		CheckRunRequest request = new CheckRunRequest("Surefile unit tests", commitSha, output);

		addCheckRun(httpclient, gson, accessToken, request, owner, repository);
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
		HttpPost post = new HttpPost("https://api.github.com/app/installations/" + installationId + "/access_tokens");
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

	private static CommitInfoResponse getCommitInfo(CloseableHttpClient httpclient, Gson gson, String accessToken,
			String owner, String repository, String topSha)
			throws JsonSyntaxException, UnsupportedOperationException, IOException {

		HttpGet get = new HttpGet("https://api.github.com/repos/" + owner + "/" + repository + "/commits/" + topSha);
		get.addHeader("Accept", "application/vnd.github.v3+json");

		try (CloseableHttpResponse response = httpclient.execute(get);
				Reader contentReader = new InputStreamReader(response.getEntity().getContent());) {
			String str = IoUtils.readerToString(contentReader);
			return gson.fromJson(str, CommitInfoResponse.class);
		}
	}

	private static Collection<FileInfo> getTreeFiles(CloseableHttpClient httpclient, Gson gson, String accessToken,
			String owner, String repository, String sha) throws IOException {

		// GET /repos/{owner}/{repo}/git/trees/{tree_sha}
		HttpGet get = new HttpGet(
				"https://api.github.com/repos/" + owner + "/" + repository + "/git/trees/" + sha + "?recursive=1");
		get.addHeader("Authorization", "token " + accessToken);
		get.addHeader("Accept", "application/vnd.github.v3+json");

		List<FileInfo> fileInfos = new ArrayList<>();
		try (CloseableHttpResponse response = httpclient.execute(get)) {
			String str = IoUtils.inputStreamToString(response.getEntity().getContent());
			// TreeInfoResponse treeInfoResponse =
			// gson.fromJson(new InputStreamReader(response.getEntity().getContent()), TreeInfoResponse.class);
			TreeInfoResponse treeInfoResponse = gson.fromJson(str, TreeInfoResponse.class);
			if (treeInfoResponse.getTreeFiles() != null) {
				for (TreeFile treeFile : treeInfoResponse.getTreeFiles()) {
					if (!TREE_TYPE.equals(treeFile.getType())) {
						// make our path a relative path from root
						fileInfos.add(new FileInfo(treeFile.getPath(), treeFile.getSha()));
					}
				}
			}
		}
		return fileInfos;
	}

	private static void addCheckRun(CloseableHttpClient httpclient, Gson gson, String accessToken,
			CheckRunRequest request, String owner, String repository) throws IOException {

		HttpPost post = new HttpPost("https://api.github.com/repos/" + owner + "/" + repository + "/check-runs");
		post.addHeader("Authorization", "token " + accessToken);
		post.addHeader("Accept", "application/vnd.github.v3+json");

		post.setEntity(new StringEntity(gson.toJson(request)));

		try (CloseableHttpResponse response = httpclient.execute(post)) {
			String str = IoUtils.inputStreamToString(response.getEntity().getContent());
			IdResponse idResponse = gson.fromJson(str, IdResponse.class);
			@SuppressWarnings("unused")
			long id = idResponse.getId();
			// Builds the JWT and serializes it to a compact, URL-safe string
			System.out.println(str);
		}
	}
}
