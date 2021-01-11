package com.j256.testcheckpublisher.github;

import com.google.gson.annotations.SerializedName;

// https://api.github.com/app/installations/42/access_tokens
public class AccessTokenRequest {
	@SerializedName("installation_id")
	final int installationId;
	final String[] repositories;

	public AccessTokenRequest(int installationId, String[] repositories) {
		this.installationId = installationId;
		this.repositories = repositories;
	}
}
