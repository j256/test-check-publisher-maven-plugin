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
	public void loadTestResults(FrameworkTestResults testResults) {

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
							"could not parse surefire XML file: " + file, writer.toString()));
				}
			}
		}
	}

	private void addTestSuite(FrameworkTestResults testResults, File file, String className, String path)
			throws Exception {

		try (Reader reader = new FileReader(file)) {
			SurefireTestSuite suite = xmlMapper.readValue(reader, SurefireTestSuite.class);
			testResults.addCounts(suite.numTests, suite.numFailures, suite.numErrors);

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

	private int findLineNumber(String fileName, String body) {
		if (body == null) {
			return DEFAULT_LINE_NUMBER;
		}

		int index = 0;
		while (true) {
			index = body.indexOf(fileName, index);
			if (index < 0) {
				return DEFAULT_LINE_NUMBER;
			}
			index += fileName.length();
			// make sure we have a ':' and a number
			if (body.length() > index) {
				if (body.charAt(index++) != ':') {
					continue;
				}
				int lineNum = 0;
				while (body.length() > index) {
					char ch = body.charAt(index++);
					if (Character.isDigit(ch)) {
						lineNum = lineNum * 10 + (ch - '0');
					} else {
						break;
					}
				}
				if (lineNum > 0) {
					return lineNum;
				}
			}
		}
	}
}
