package com.j256.testcheckpublisher.plugin.frameworks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
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
import com.j256.testcheckpublisher.plugin.frameworks.SurefireTestSuite.Problem;
import com.j256.testcheckpublisher.plugin.frameworks.SurefireTestSuite.TestCase;
import com.j256.testcheckpublisher.plugin.frameworks.TestFileResult.TestLevel;

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
	private final int DEFAULT_MAX_LINES_READ = 5000;

	private int maxLinesRead = DEFAULT_MAX_LINES_READ;

	@Override
	public void loadTestResults(FrameworkTestResults testResults, File testReportDir, File sourceDir, Log log) {

		testReportDir = checkDir("Surefire test report", testReportDir, SUREFIRE_DIR, log);
		if (testReportDir == null) {
			return;
		}
		sourceDir = checkDir("Source", sourceDir, ".", log);
		Map<String, File> fileNameMap;
		if (sourceDir == null) {
			fileNameMap = Collections.emptyMap();
		} else {
			fileNameMap = getFileNameMap(sourceDir);
		}

		File[] files = testReportDir.listFiles();
		if (files == null || files.length == 0) {
			log.warn("The surefire test report directory has no files in it: " + testReportDir);
			return;
		}

		testResults.setName("Surefire test results");
		for (File file : files) {
			Matcher matcher = XML_PATTERN.matcher(file.getName());
			if (!matcher.matches()) {
				continue;
			}
			String className = matcher.group(1);
			String fileName = matcher.group(2) + ".java";
			String path = className.replace('.', '/');
			SurefireTestSuite suite;
			try {
				try (Reader reader = new FileReader(file)) {
					suite = xmlMapper.readValue(reader, SurefireTestSuite.class);
				}
			} catch (Exception e) {
				log.error("Could not parse surefire XML file: " + file, e);
				continue;
			}
			addTestSuite(testResults, suite, className, path, fileName, fileNameMap.get(fileName), log);
		}
		log.debug("Added results: " + testResults);
	}

	/**
	 * For testing purposes.
	 */
	public void setMaxLinesRead(int maxLinesRead) {
		this.maxLinesRead = maxLinesRead;
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

	private void addTestSuite(FrameworkTestResults testResults, SurefireTestSuite suite, String className, String path,
			String fileName, File sourceFile, Log log) {

		int lineNumber;
		testResults.addCounts(suite.getNumTests(), suite.getNumFailures(), suite.getNumErrors(), suite.getNumSkipped());

		MutableInteger sourceFileMissingCounter = new MutableInteger();
		MutableInteger tooLongCounter = new MutableInteger();
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
					lineNumber = findMethodLineNumber(fileName, test.getName(), sourceFile, tooLongCounter,
							sourceFileMissingCounter, log);
					testResults.addFileResult(new TestFileResult(path, lineNumber, lineNumber, TestLevel.NOTICE,
							test.timeSeconds, className + "." + test.getName(), "succeeded, no errors", null));
				}
				continue;
			}

			// look to find if we have file:line format
			lineNumber = findErrorLineNumber(className, problem.body);
			if (lineNumber == DEFAULT_LINE_NUMBER) {
				lineNumber = findMethodLineNumber(fileName, test.getName(), sourceFile, tooLongCounter,
						sourceFileMissingCounter, log);
			}
			testResults.addFileResult(new TestFileResult(path, lineNumber, lineNumber, level, test.timeSeconds,
					className + "." + test.getName(), problem.type + ": " + problem.message, problem.body));
		}

		// print a message if the source file is too long and we didn't match some methods
		if (tooLongCounter.count > 0) {
			log.debug("Stopped looking for " + tooLongCounter.count + " methods after processing " + maxLinesRead
					+ " lines from " + sourceFile);
		}
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

	private int findMethodLineNumber(String fileName, String methodName, File sourceFile, MutableInteger tooLongCounter,
			MutableInteger sourceFileMissingCounter, Log log) {

		if (sourceFile == null) {
			if (sourceFileMissingCounter.count == 0) {
				log.debug("Unknown test source-file name: " + fileName);
				sourceFileMissingCounter.count = 1;
			}
			return DEFAULT_LINE_NUMBER;
		}
		if (!sourceFile.isFile()) {
			if (sourceFileMissingCounter.count == 0) {
				log.debug("Source-file is not a file: " + sourceFile);
				sourceFileMissingCounter.count = 1;
			}
		}
		if (!sourceFile.canRead()) {
			if (sourceFileMissingCounter.count == 0) {
				log.debug("Source-file is not readable: " + sourceFile);
				sourceFileMissingCounter.count = 1;
			}
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
					if (lineNumber > maxLinesRead) {
						tooLongCounter.count++;
						return DEFAULT_LINE_NUMBER;
					}
				}
			}
		} catch (IOException ioe) {
			log.error("Could not read source-file " + sourceFile + " to find method: " + methodName, ioe);
			return DEFAULT_LINE_NUMBER;
		}
	}

	private static class MutableInteger {
		int count;
	}
}
