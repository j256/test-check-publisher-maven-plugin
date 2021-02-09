package com.j256.testcheckpublisher.plugin.gitcontext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.Queue;

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
	public void testPattern1() {
		CommandLineGitContextFinder finder = new CommandLineGitContextFinder();
		Queue<String> queue = new LinkedList<>();
		queue.add("git@github.com:j256/test-check-publisher-maven-plugin.git");
		queue.add("commit 0acbb6a4b964ef147a1fba7e071a4f17da46bba8 (HEAD -> gw-better-display-output)");
		finder.setTestFirstLines(queue);
		GitContext context = finder.findContext();
		assertNotNull(context);
		assertEquals("j256", context.getOwner());
		assertEquals("test-check-publisher-maven-plugin", context.getRepository());
	}

	@Test
	public void testPattern2() {
		CommandLineGitContextFinder finder = new CommandLineGitContextFinder();
		Queue<String> queue = new LinkedList<>();
		queue.add("ssh://git@github.com/j256/test-check-publisher-maven-plugin.git");
		queue.add("commit 0acbb6a4b964ef147a1fba7e071a4f17da46bba8 (HEAD -> gw-better-display-output)");
		finder.setTestFirstLines(queue);
		GitContext context = finder.findContext();
		assertNotNull(context);
		assertEquals("j256", context.getOwner());
		assertEquals("test-check-publisher-maven-plugin", context.getRepository());
	}
}
