package com.j256.testcheckpublisher.plugin.gitcontext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.j256.testcheckpublisher.plugin.gitcontext.GitContextFinder.GitContext;

public class GithubActionContextFinderTest {

	@Test
	public void testStuff() {
		GithubActionContextFinder finder = new GithubActionContextFinder();
		assertFalse(finder.isRunning());
		GitContext gitContext = finder.findContext();
		assertNull(gitContext);
	}

	@Test
	public void testPattern1() {
		GithubActionContextFinder finder = new GithubActionContextFinder();
		finder.setGithubAction("__run_2");
		String owner = "j256";
		finder.setGithubRepositoryOwner(owner);
		String repository = "simplejmx";
		finder.setGithubOwnerAndRepository(owner + "/" + repository);
		finder.setGithubSha("sha32prje2prjp2oerj2");
		GitContext context = finder.findContext();
		assertNotNull(context);
		assertEquals(owner, context.getOwner());
		assertEquals(repository, context.getRepository());
	}
}
