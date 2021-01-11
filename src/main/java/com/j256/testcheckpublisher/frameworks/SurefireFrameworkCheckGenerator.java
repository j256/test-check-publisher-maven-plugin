package com.j256.testcheckpublisher.frameworks;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.j256.testcheckpublisher.FileInfo;
import com.j256.testcheckpublisher.IoUtils;
import com.j256.testcheckpublisher.frameworks.SurefireTestSuite.Problem;
import com.j256.testcheckpublisher.frameworks.SurefireTestSuite.TestCase;
import com.j256.testcheckpublisher.github.CheckRunRequest.CheckRunAnnotation;
import com.j256.testcheckpublisher.github.CheckRunRequest.CheckRunOutput;
import com.j256.testcheckpublisher.github.CheckRunRequest.Level;

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
	public CheckRunOutput createRequest(String sha, Collection<FileInfo> fileInfos) throws Exception {

		CheckRunOutput output = new CheckRunOutput("", "", "");

		/*
		 * Create a map of path portions to file names, the idea being that we may have classes not laid out in a nice
		 * hierarchy and we don't want to read all files looking for package ...
		 */
		Map<String, FileInfo> nameMap = new HashMap<>();
		for (FileInfo fileInfo : fileInfos) {
			String path = fileInfo.getPath();
			nameMap.put(path, fileInfo);
			nameMap.put(fileInfo.getName(), fileInfo);
			int index = 0;
			while (true) {
				int nextIndex = path.indexOf(File.separatorChar, index);
				if (nextIndex < 0) {
					break;
				}
				index = nextIndex + 1;
				nameMap.put(path.substring(index), fileInfo);
			}
			// should be just the name
			nameMap.put(path.substring(index), fileInfo);
		}

		File dir = new File(SUREFIRE_DIR);
		for (File file : dir.listFiles()) {
			Matcher matcher = XML_PATTERN.matcher(file.getName());
			if (!matcher.matches()) {
				continue;
			}

			String className = matcher.group(1);
			FileInfo fileInfo = mapFileByClass(nameMap, className);
			if (fileInfo == null) {
				// XXX: could not locate this file
				System.err.println("could not locate class: " + className);
			} else {
				addTestSuite(output, file, className, fileInfo);
			}
		}

		String title = output.getTestCount() + " tests, " + output.getErrorCount() + " errors, "
				+ output.getFailureCount() + " failures";
		output.setTitle(title);

		return output;
	}

	private FileInfo mapFileByClass(Map<String, FileInfo> nameMap, String className) {
		String path = className.replace('.', File.separatorChar) + ".java";

		FileInfo result = nameMap.get(path);
		if (result != null) {
			return result;
		}

		int index = 0;
		while (true) {
			int nextIndex = path.indexOf(File.separatorChar, index);
			if (nextIndex < 0) {
				break;
			}
			index = nextIndex + 1;
			result = nameMap.get(path.substring(index));
			if (result != null) {
				return result;
			}
		}
		// should be just the name
		return nameMap.get(path.substring(index));
	}

	private void addTestSuite(CheckRunOutput output, File file, String className, FileInfo fileInfo) throws Exception {
		try (Reader reader = new FileReader(file)) {
			String str = IoUtils.readerToString(reader);
			SurefireTestSuite suite = xmlMapper.readValue(str, SurefireTestSuite.class);
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
						output.addAnnotation(new CheckRunAnnotation(fileInfo.getPath(), 1, 1, Level.NOTICE,
								test.getName() + " succeeded", "no errors", null));
					}
					continue;
				}

				int lineNumber = findLineNumber(className, problem.body);
				if (lineNumber == DEFAULT_LINE_NUMBER) {
					lineNumber = findLineNumber(fileInfo.getName(), problem.body);
				}
				output.addAnnotation(new CheckRunAnnotation(fileInfo.getPath(), lineNumber, lineNumber, level,
						test.getClassName() + "." + test.getName() + " failed test",
						problem.type + ": " + problem.message, problem.body));

			}
		}
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
