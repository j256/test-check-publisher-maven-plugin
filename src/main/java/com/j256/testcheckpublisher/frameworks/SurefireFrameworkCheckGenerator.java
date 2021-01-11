package com.j256.testcheckpublisher.frameworks;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.j256.testcheckpublisher.frameworks.SurefireTestSuite.Problem;
import com.j256.testcheckpublisher.frameworks.SurefireTestSuite.TestCase;
import com.j256.testcheckpublisher.github.CheckRunRequest.CheckRunAnnotation;
import com.j256.testcheckpublisher.github.CheckRunRequest.CheckRunOutput;
import com.j256.testcheckpublisher.github.CheckRunRequest.Level;
import com.j256.testcheckpublisher.github.TreeFile;

/**
 * Generate a check-run object from surefire XML files.
 * 
 * @author graywatson
 */
public class SurefireFrameworkCheckGenerator implements FrameworkCheckGenerator {

	private final XmlMapper xmlMapper = new XmlMapper();
	private final String SUREFIRE_DIR = "target/surefire-reports";
	private final Pattern XML_PATTERN = Pattern.compile("TEST-(.*)\\.xml");
	private final boolean SHOW_NOTICE = true;
	private final int DEFAULT_LINE_NUMBER = 1;

	@Override
	public CheckRunOutput createRequest(String sha, TreeFile[] treeFiles) throws Exception {

		CheckRunOutput output = new CheckRunOutput("Surefire test run", null, "other text");

		String summary = output.getTestCount() + " tests, " + output.getErrorCount() + " errors, "
				+ output.getFailureCount() + " failures";
		output.setSummary(summary);

		/*
		 * Create a map of path portions to file names, the idea being that we may have classes not laid out in a nice
		 * hierarchy and we don't want to read all files looking for package ...
		 */
		Map<String, String> nameMap = new HashMap<>();
		for (TreeFile treeFile : treeFiles) {
			String path = treeFile.getPath();
			int index = 0;
			while (true) {
				int nextIndex = path.indexOf(File.pathSeparator, index);
				if (nextIndex < 0) {
					break;
				}
				index = nextIndex + 1;
				nameMap.put(path.substring(index), path);
			}
			// should be just the name
			nameMap.put(path.substring(index), path);
		}

		File dir = new File(SUREFIRE_DIR);
		for (File file : dir.listFiles()) {
			Matcher matcher = XML_PATTERN.matcher(file.getName());
			if (!matcher.matches()) {
				continue;
			}

			String className = matcher.group(1);
			String fileName = fileFileMap(nameMap, className);
			if (fileName == null) {
				// XXX: could not locate this file
				System.err.println("could not locate class: " + className);
			} else {
				addTestSuite(output, file, className, fileName);
			}
		}

		return output;
	}

	private String fileFileMap(Map<String, String> nameMap, String className) {
		String path = className.replace('.', File.separatorChar);

		int index = 0;
		while (true) {
			int nextIndex = path.indexOf(File.pathSeparator, index);
			if (nextIndex < 0) {
				break;
			}
			index = nextIndex + 1;
			String result = nameMap.get(path.substring(index));
			if (result != null) {
				return result;
			}
		}
		// should be just the name
		return nameMap.get(path.substring(index));
	}

	private void addTestSuite(CheckRunOutput output, File file, String className, String fileName) throws Exception {
		try (Reader reader = new FileReader(file)) {
			SurefireTestSuite suite = xmlMapper.readValue(reader, SurefireTestSuite.class);
			output.addCounts(suite.numTests, suite.numFailures, suite.numErrors);

			for (TestCase test : suite.getTestcases()) {

				// need to find the path in question in the list of tree-files
				// go through the body and find the first line mentioning a particular test (closest to failure)
				// get line number if possible otherwise use 1

				Level level;
				Problem failure = test.getFailure();
				Problem error = test.getError();
				Problem problem;
				if (failure != null) {
					problem = failure;
					level = Level.FAILURE;
				} else if (error != null) {
					problem = error;
					level = Level.ERROR;
				} else {
					if (SHOW_NOTICE) {
						output.addAnnotation(new CheckRunAnnotation(fileName, 1, 1, Level.NOTICE, "no errors",
								test.getName() + " succeeded", null));
					}
					continue;
				}

				int lineNumber = findLineNumber(className, problem.body);
				output.addAnnotation(new CheckRunAnnotation(fileName, lineNumber, lineNumber, level, problem.message,
						test.getName() + " failed test", problem.body));

			}
		}
	}

	private int findLineNumber(String className, String body) {
		if (body == null) {
			return DEFAULT_LINE_NUMBER;
		}

		int index = 0;
		while (true) {
			index = body.indexOf(className, index);
			if (index < 0) {
				return DEFAULT_LINE_NUMBER;
			}
			index += className.length();
			// make sure we have a ':' and a number
			index++;
			if (body.length() > index) {
				if (body.charAt(index++) != ':') {
					continue;
				}
				int lineNum = 0;
				while (body.length() > index) {
					char ch = body.charAt(index++);
					if (Character.isDigit(ch)) {
						lineNum = lineNum * 10 + (ch - '0');
					} else if (lineNum > 0) {
						return lineNum;
					}
				}
			}
		}
	}
}
