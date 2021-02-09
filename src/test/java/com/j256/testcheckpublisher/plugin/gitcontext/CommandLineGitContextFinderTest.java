package com.j256.testcheckpublisher.plugin.gitcontext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.j256.testcheckpublisher.plugin.gitcontext.GitContextFinder.GitContext;

public class CommandLineGitContextFinderTest {

	@Test
	public void testStuff() {
		CommandLineGitContextFinder finder = new CommandLineGitContextFinder();
		assertTrue(finder.isRunning());
		GitContext context = finder.findContext();
		assertNotNull(context);
		assertEquals("j256", context.getOwner());
		assertEquals("test-check-publisher-maven-plugin", context.getRepository());
	}

	@Test
	public void testPatterns() {
		CommandLineGitContextFinder finder = new CommandLineGitContextFinder();
		finder.setTestFirstLine("ssh://git@github.com/j256/test-check-publisher-maven-plugin.git");
		GitContext context = finder.findContext();
		assertNotNull(context);
		assertEquals("j256", context.getOwner());
		assertEquals("test-check-publisher-maven-plugin", context.getRepository());

		finder.setTestFirstLine("git@github.com:j256/test-check-publisher-maven-plugin.git");
		context = finder.findContext();
		assertNotNull(context);
		assertEquals("j256", context.getOwner());
		assertEquals("test-check-publisher-maven-plugin", context.getRepository());
	}
}
