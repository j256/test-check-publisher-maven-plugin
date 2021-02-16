Test Check Publisher Maven Plugin
=================================

This maven plugin is designed to take tests results produced by continuous integration and publish
them to github as a "check" annotation on a particular commit.  For the full instructions about how
to get this working, see the
[Test Check Publisher Github application](https://github.com/apps/test-check-publisher).

* The source code be found on the [git repository](https://github.com/j256/test-check-publisher-maven-plugin). [![CircleCI](https://circleci.com/gh/j256/test-check-publisher-maven-plugin.svg?style=svg)](https://circleci.com/gh/j256/test-check-publisher-maven-plugin) [![CodeCov](https://img.shields.io/codecov/c/github/j256/test-check-publisher-maven-plugin.svg)](https://codecov.io/github/j256/test-check-publisher-maven-plugin/)
* Maven packages are published via [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.j256.testcheckpublisher/test-check-publisher-maven-plugin/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/com.j256.testcheckpublisher/test-check-publisher-maven-plugin/)
* Documentation: [![javadoc](https://javadoc.io/badge2/com.j256.testcheckpublisher/test-check-publisher-maven-plugin/javadoc.svg)](https://javadoc.io/doc/com.j256.testcheckpublisher/test-check-publisher-maven-plugin)

You need to add the test-check-publisher integration to your repository and then add something like
the following to your pom.xml file:

	<build>
		...
		<plugins>
			...
			<plugin>
				<groupId>com.j256.testcheckpublisher</groupId>
				<artifactId>test-check-publisher-maven-plugin</artifactId>
				<version>1.3</version>
			</plugin>

After you have run your unit tests, you need to execute:

	mvn test-check-publisher:publish

# Examples

Here are some examples of output from github:

* [Cloudwatch Logback Appender unit test error](https://github.com/j256/cloudwatch-logback-appender/runs/1865637224)

# Extensions

Right now the plugin is pretty limited to Java/surefire results but the system has been built to be more generic.  If
you have a testing framework that you'd like it to support, please
[add an issue](https://github.com/j256/test-check-publisher-maven-plugin/issues).  All that is required is the reading
in test results and posting something like the following JSON entity to the server.

```
{
  "magic": 237347409389423823,
  "owner": "owner",
  "repository": "repo",
  "commitSha": "sha",
  "secret": "secret env value from installation",
  "results": {
    "name": "Test results name",
    "numTests": 1,
    "numFailures": 3,
    "numErrors": 2,
    "fileResults": [
      {
        "path": "path/to/file1.java",
        "startLineNumber": 101,
        "endLineNumber": 101,
        "testLevel": "ERROR",
        "timeSeconds": 0.1,
        "testName": "testName",
        "message": "assert error",
        "details": "more details here"
      }
    ],
  }
}
```

# Screenshots

The following image shows an example of the output that you would see on Github associated with a particular commit.
This could provide more information if your Continuous Integration run failed because of a unit test issue.

![Example of output showing how unit test lines in the commit are annotated.](https://marketplace-screenshots.githubusercontent.com/9010/2d1d8680-6b1f-11eb-9f76-cce7353daef8)
![If a unit test fails that isn't in the commit, the file and line are displayed above for reference.](https://marketplace-screenshots.githubusercontent.com/9010/3ee24100-60be-11eb-8cfd-415a6caad49a)

# Configuration

The maven plugin supports a couple of configuration parameters:

	<plugin>
		<groupId>com.j256.testcheckpublisher</groupId>
		<artifactId>test-check-publisher-maven-plugin</artifactId>
		<configuration>
			<field>value</field>
		</configuration>
	</plugin>

| Field | Default | Description |
| ----- | ------- | ----------- |
| serverUrl | See TestCheckPubMojo.java | URL of the server if you are running your own instance. |
| maxNumResults | 50 | Maximum number of check results to post up to an internal limit of 500. |
| secretEnvName | TEST_CHECK_PUBLISHER_SECRET | Name of the environmental variable holding the secret. |
| secretValue | none | Should not be used for security reasons.  See secretEnvName. |
| framework | SUREFIRE | Name of the framework to use to read in test results. |
| context | detected | Name of the context to use to find git owner/repo/commit-sha. |
| testReportDir | none | Directory holding the tests.  The framework can have a default. |
| sourceDir | . | Directory holding the sources so we can find file paths. |
| verbose | false | Verbose log output if mvn -X is used. |
| format | none | Comma separated tokens that affect the resulting github checks format.  See below. |
| ignorePass | false | Do not post any information about tests that pass. |

# Format Tokens

In the configuration for the plugin, you can specify a comma separated list of tokens which affect the resulting
check annotations and details formatting. 

The following tokens are supported:

| Token | Default | Description |
| ----- | ------- | ----------- |
| nodetails | false | Do not write any entries into the details section at the top. |
| noannotate | false | Don't write any annotations.  Only update the details section at the top. |
| nopass | false | Do not emit annotations or details if the tests pass. |
| alwaysannotate | false | Always emit annotations even if the test was not part of the commit. |
| noemoji | false | Do not show the emoji at the front of each details section. |
| passdetails | false | Write details for passing tests as well. |

There are two different checks output sections.  Closer to the top of the page is a details section which is used to
provide details of the check in markdown.  Below it is the annotations section where file and line-number is annotated
with the results of a test.  The problem is that not all of the files and line-numbers are shown in the commit.  You
might make a change to your code and fail a test that hasn't been touched in months and the annotation will look good
but the link to the file/line will be broken.  The server tries to detect this and writes some of the results into the
details section and some into the annotations based on whether or not it was involved in the commit.  It's not perfect.

If you want all of your test results in the annotations section then you should use the `nodetails` token.  If you
think that the details looks good, you might try `noannotate`.  If you want to see the passing tests as well in the
details section then you might want to add `alldetails`.
