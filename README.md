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
				<!-- check for a later version -->
				<version>1.2</version>
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
