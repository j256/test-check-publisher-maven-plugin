package com.j256.testcheckpublisher.plugin.frameworks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.logging.Log;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.j256.testcheckpublisher.plugin.frameworks.FrameworkTestResults.TestFileResult;
import com.j256.testcheckpublisher.plugin.frameworks.FrameworkTestResults.TestFileResult.TestLevel;
import com.j256.testcheckpublisher.plugin.frameworks.SurefireTestSuite.Problem;
import com.j256.testcheckpublisher.plugin.frameworks.SurefireTestSuite.TestCase;

/**
 * Generate a check-run object from surefire XML files.
 * 
 * @author graywatson
 */
public class SurefireFrameworkCheckGenerator implements FrameworkCheckGenerator {

	private final ObjectMapper xmlMapper =
			new XmlMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	private final String SUREFIRE_DIR = "target/surefire-reports";
	private final Pattern XML_PATTERN = Pattern.compile("TEST-(.*?([^.]+))\\.xml");
	private final boolean SHOW_NOTICE = true;
	private final int DEFAULT_LINE_NUMBER = 1;
	// try to limit the amount of IO
	private final int MAX_LINES_READ = 1000;

	@Override
	public void loadTestResults(FrameworkTestResults testResults, File testReportDir, File sourceDir, Log log) {

		testReportDir = checkDir("Surefire test report", testReportDir, SUREFIRE_DIR, log);
		sourceDir = checkDir("Source", sourceDir, ".", log);

		Map<String, File> fileNameMap = getFileNameMap(sourceDir);

		testResults.setName("Surefire test results");

		File[] files = testReportDir.listFiles();
		if (files == null || files.length == 0) {
			log.warn("The surefire test report directory has no files in it: " + testReportDir);
			return;
		}
		for (File file : files) {
			Matcher matcher = XML_PATTERN.matcher(file.getName());
			if (matcher.matches()) {
				String className = matcher.group(1);
				String fileName = matcher.group(2) + ".java";
				String path = classToPath(className);
				try {
					addTestSuite(testResults, file, className, path, fileName, fileNameMap.get(fileName), log);
				} catch (Exception e) {
					StringWriter writer = new StringWriter();
					e.printStackTrace(new PrintWriter(writer));
					log.error("Could not parse surefire XML file: " + e);
					log.debug(e);
				}
			}
		}
		log.debug("Added results: " + testResults);
	}

	private File checkDir(String label, File dir, String defaultPath, Log log) {
		if (dir == null) {
			dir = new File(defaultPath);
		}
		if (!dir.exists()) {
			log.error(label + " dir does not exist: " + dir);
			return null;
		}
		if (!dir.isDirectory()) {
			log.error(label + " dir is not a directory: " + dir);
			return null;
		}
		return dir;
	}

	/**
	 * Run through the file-system and create a map of file-name -> File.
	 */
	private Map<String, File> getFileNameMap(File sourceDir) {
		Map<String, File> fileNameMap = new HashMap<>();
		Queue<File> dirQueue = new LinkedList<>();
		dirQueue.add(sourceDir);
		while (dirQueue.size() > 0) {
			File dir = dirQueue.poll();
			for (File file : dir.listFiles()) {
				if (file.isDirectory()) {
					dirQueue.add(file);
				} else {
					fileNameMap.put(file.getName(), file);
				}
			}
		}
		return fileNameMap;
	}

	private void addTestSuite(FrameworkTestResults testResults, File file, String className, String path,
			String fileName, File sourceFile, Log log) throws Exception {

		int lineNumber;
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
						lineNumber = findMethodLineNumber(fileName, test.getName(), sourceFile, log);
						testResults.addFileResult(new TestFileResult(path, lineNumber, TestLevel.NOTICE,
								test.timeSeconds, className + "." + test.getName(), "succeeded, no errors", null));
					}
					continue;
				}

				// look to find if we have file:line format
				lineNumber = findErrorLineNumber(className, problem.body);
				if (lineNumber == DEFAULT_LINE_NUMBER) {
					lineNumber = findMethodLineNumber(fileName, test.getName(), sourceFile, log);
				}
				testResults.addFileResult(new TestFileResult(path, lineNumber, level, test.timeSeconds,
						className + "." + test.getName(), problem.type + ": " + problem.message, problem.body));
			}
		}
	}

	private String classToPath(String className) {
		return className.replace('.', '/');
	}

	private int findErrorLineNumber(String className, String body) {
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

	private int findMethodLineNumber(String fileName, String methodName, File sourceFile, Log log) {
		if (sourceFile == null) {
			log.debug("Unknown test source-file name: " + fileName);
			return DEFAULT_LINE_NUMBER;
		}

		// this looks for the method name but not if there is a * in the prefix which may mean comment
		Pattern methodPattern = Pattern.compile("(^|[^\\*]*\\s)" + methodName + "\\s*\\(.*");
		try {
			int lineNumber = 0;
			try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile));) {
				while (true) {
					String line = reader.readLine();
					if (line == null) {
						return DEFAULT_LINE_NUMBER;
					}
					lineNumber++;
					Matcher matcher = methodPattern.matcher(line);
					if (matcher.matches()) {
						return lineNumber;
					}
					if (lineNumber > MAX_LINES_READ) {
						log.debug("Stopped looking for method " + methodName + " after processing " + MAX_LINES_READ
								+ " from " + sourceFile);
						return DEFAULT_LINE_NUMBER;
					}
				}
			}
		} catch (IOException ioe) {
			log.error("Could not read source-file " + sourceFile + " to find method: " + methodName);
			return DEFAULT_LINE_NUMBER;
		}
	}
}
