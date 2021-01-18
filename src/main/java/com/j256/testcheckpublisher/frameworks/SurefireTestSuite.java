package com.j256.testcheckpublisher.frameworks;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

/**
 * XML surefire results file.
 * 
 * @author graywatson
 */
@JacksonXmlRootElement(localName = "testsuite")
public class SurefireTestSuite {

	@JacksonXmlProperty(isAttribute = true)
	private String name;
	@JacksonXmlProperty(isAttribute = true, localName = "time")
	private float timeSeconds;
	@JacksonXmlProperty(isAttribute = true, localName = "tests")
	private int numTests;
	@JacksonXmlProperty(isAttribute = true, localName = "errors")
	private int numErrors;
	@JacksonXmlProperty(isAttribute = true, localName = "failures")
	private int numFailures;

	@JacksonXmlProperty(localName = "testcase")
	@JacksonXmlElementWrapper(useWrapping = false)
	TestCase[] testcases;

	public String getName() {
		return name;
	}

	public float getTimeSeconds() {
		return timeSeconds;
	}

	public int getNumTests() {
		return numTests;
	}

	public int getNumErrors() {
		return numErrors;
	}

	public int getNumFailures() {
		return numFailures;
	}

	public TestCase[] getTestcases() {
		return testcases;
	}

	@JacksonXmlRootElement(localName = "testcase")
	public static class TestCase {
		@JacksonXmlProperty(isAttribute = true)
		String name;
		@JacksonXmlProperty(isAttribute = true, localName = "classname")
		String className;
		@JacksonXmlProperty(isAttribute = true, localName = "time")
		float timeSeconds;
		@JacksonXmlProperty
		Problem error;
		@JacksonXmlProperty
		Problem failure;

		public String getName() {
			return name;
		}

		public String getClassName() {
			return className;
		}

		public float getTimeSeconds() {
			return timeSeconds;
		}

		public Problem getError() {
			return error;
		}

		public Problem getFailure() {
			return failure;
		}
	}

	@JacksonXmlRootElement
	public static class Problem {
		@JacksonXmlProperty(isAttribute = true)
		String message;
		@JacksonXmlProperty(isAttribute = true)
		String type;
		@JacksonXmlText
		String body;

		public String getMessage() {
			return message;
		}

		public String getType() {
			return type;
		}

		public String getBody() {
			return body;
		}
	}
}
