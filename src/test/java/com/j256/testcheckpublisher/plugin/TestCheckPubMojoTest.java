package com.j256.testcheckpublisher.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Ignore;

@Ignore("integreation")
public class TestCheckPubMojoTest {

	public static void main(String[] args) throws MojoExecutionException {
		TestCheckPubMojo mojo = new TestCheckPubMojo();
		mojo.setSecretValue("foo");
		mojo.execute();
	}
}
