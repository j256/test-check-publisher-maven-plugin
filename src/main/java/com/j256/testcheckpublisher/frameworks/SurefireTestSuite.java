package com.j256.testcheckpublisher.frameworks;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

/**
 * XML surefire output file.
 * 
 * @author graywatson
 */
@JacksonXmlRootElement(localName = "testsuite")
public class SurefireTestSuite {

	@JacksonXmlProperty(isAttribute = true)
	String name;
	@JacksonXmlProperty(isAttribute = true, localName = "time")
	float timeSeconds;
	@JacksonXmlProperty(isAttribute = true, localName = "tests")
	int numTests;
	@JacksonXmlProperty(isAttribute = true, localName = "errors")
	int numErrors;
	@JacksonXmlProperty(isAttribute = true, localName = "failures")
	int numFailures;

	@JacksonXmlProperty
	Property[] properties;
	// @JacksonXmlProperty
	// TestCase testcase;
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

	public Property[] getProperties() {
		return properties;
	}

	public TestCase[] getTestcases() {
		return testcases;
	}

	public static class Property {
		@JacksonXmlProperty(isAttribute = true)
		String name;
		@JacksonXmlProperty(isAttribute = true)
		String value;

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}
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
