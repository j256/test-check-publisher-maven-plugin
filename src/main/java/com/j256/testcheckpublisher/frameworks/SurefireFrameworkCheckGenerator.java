package com.j256.testcheckpublisher.frameworks;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.j256.testcheckpublisher.frameworks.FrameworkTestResults.TestFileResult;
import com.j256.testcheckpublisher.frameworks.FrameworkTestResults.TestFileResult.TestLevel;
import com.j256.testcheckpublisher.frameworks.SurefireTestSuite.Problem;
import com.j256.testcheckpublisher.frameworks.SurefireTestSuite.TestCase;

/**
 * Generate a check-run object from surefire XML files.
 * 
 * @author graywatson
 */
public class SurefireFrameworkCheckGenerator implements FrameworkCheckGenerator {

	private final ObjectMapper xmlMapper =
			new XmlMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	private final String SUREFIRE_DIR = "target/surefire-reports";
	private final Pattern XML_PATTERN = Pattern.compile("TEST-(.*)\\.xml");
	private final boolean SHOW_NOTICE = true;
	private final int DEFAULT_LINE_NUMBER = 1;

	@Override
	public void loadTestResults(FrameworkTestResults testResults, File testWorkingDir) {

		if (testWorkingDir == null) {
			testWorkingDir = new File(SUREFIRE_DIR);
		}

		if (!testWorkingDir.exists()) {
			testResults.addFileResult(new TestFileResult(testWorkingDir.getPath(), 1, TestLevel.ERROR, 0.0F,
					"test.working.dir", "Test working dir does not exist: " + testWorkingDir, null));
			return;
		}
		if (!testWorkingDir.isDirectory()) {
			testResults.addFileResult(new TestFileResult(testWorkingDir.getPath(), 1, TestLevel.ERROR, 0.0F,
					"test.working.dir", "Test working dir is not a directory: " + testWorkingDir, null));
			return;
		}

		testResults.setName("Surefire test results");

		// XXX: should we locate the surefire-dir?
		File dir = new File(SUREFIRE_DIR);
		for (File file : dir.listFiles()) {
			Matcher matcher = XML_PATTERN.matcher(file.getName());
			if (matcher.matches()) {
				String className = matcher.group(1);
				String path = classToPath(className);
				try {
					addTestSuite(testResults, file, className, path);
				} catch (Exception e) {
					StringWriter writer = new StringWriter();
					e.printStackTrace(new PrintWriter(writer));
					testResults.addFileResult(new TestFileResult(path, 1, TestLevel.ERROR, 0.0F, className,
							"Could not parse surefire XML file: " + file, writer.toString()));
				}
			}
		}
	}

	private void addTestSuite(FrameworkTestResults testResults, File file, String className, String path)
			throws Exception {

		try (Reader reader = new FileReader(file)) {
			SurefireTestSuite suite = xmlMapper.readValue(reader, SurefireTestSuite.class);
			testResults.addCounts(suite.getNumTests(), suite.getNumFailures(), suite.getNumErrors());

			for (TestCase test : suite.getTestcases()) {

				Problem failure = test.getFailure();
				Problem error = test.getError();

				TestLevel level;
				Problem problem;
				if (failure != null) {
					problem = failure;
					level = TestLevel.FAILURE;
				} else if (error != null) {
					problem = error;
					level = TestLevel.ERROR;
				} else {
					if (SHOW_NOTICE) {
						testResults.addFileResult(new TestFileResult(path, 1, TestLevel.NOTICE, test.timeSeconds,
								className + "." + test.getName(), "succeeded, no errors", null));
					}
					continue;
				}

				// look to find if we have file:line format
				int lineNumber = findLineNumber(className, problem.body);
				testResults.addFileResult(new TestFileResult(path, lineNumber, level, test.timeSeconds,
						className + "." + test.getName(), problem.type + ": " + problem.message, problem.body));
			}
		}
	}

	private String classToPath(String className) {
		return className.replace('.', '/');
	}

	// xxx not working, i think it is looking for com.j256.Foo:number
	private int findLineNumber(String className, String body) {
		if (body == null) {
			return DEFAULT_LINE_NUMBER;
		}

		int index = 0;
		while (true) {
			// at com.j256.testcheckpublisher.TestCheckPubMainTest.test(TestCheckPubMainTest.java:11)
			index = body.indexOf(className, index);
			if (index < 0) {
				return DEFAULT_LINE_NUMBER;
			}
			index += className.length();
			// make sure we have a ':' and a number
			boolean number = false;
			int lineNum = 0;
			while (index < body.length()) {
				char ch = body.charAt(index++);
				if (number) {
					if (Character.isDigit(ch)) {
						lineNum = lineNum * 10 + (ch - '0');
						continue;
					} else {
						break;
					}
				}
				if (ch == ':') {
					number = true;
				} else if (Character.isWhitespace(ch)) {
					break;
				}
			}
			if (lineNum > 0) {
				return lineNum;
			}
		}
	}
}
