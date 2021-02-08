Test Check Publisher Maven Plugin
=================================

This maven plugin is designed to take tests results produced by continuous integration and publish
them to github as a "check" annotation on a particular commit.  For the full instructions about how
to get this working, see the
[Test Check Publisher Github application](https://github.com/apps/test-check-publisher).

* The source code be found on the [git repository](https://github.com/j256/test-check-publisher-maven-plugin).  	 [![CircleCI](https://circleci.com/gh/j256/test-check-publisher-maven-plugin.svg?style=svg)](https://circleci.com/gh/j256/test-check-publisher-maven-plugin) [![CodeCov](https://img.shields.io/codecov/c/github/j256/test-check-publisher-maven-plugin.svg)](https://codecov.io/github/j256/test-check-publisher-maven-plugin/)
* Maven packages are published via [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.j256.testcheckpublisher/test-check-publisher-maven-plugin/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/com.j256.testcheckpublisher/test-check-publisher-maven-plugin/) [![javadoc](https://javadoc.io/badge2/com.j256.testcheckpublisher/test-check-publisher-maven-plugin/javadoc.svg)](https://javadoc.io/doc/com.j256.testcheckpublisher/test-check-publisher-maven-plugin)

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
				<version>0.6</version>
			</plugin>

After you have run your unit tests, you need to execute:

	mvn test-check-publisher:publish

# Extensions

Right now the plugin is pretty limited to Java/surefire results but it has been built to be more generic.  If you
have a testing framework that you'd like it to support, please
[add an issue](https://github.com/j256/test-check-publisher-maven-plugin/issues).

# Screenshots

The following image shows an example of the output that you would see on Github associated with a particular commit.
This could provide more information if your Continuous Integration run failed because of a unit test issue.

![Example of output](https://marketplace-screenshots.githubusercontent.com/9010/3ee24100-60be-11eb-8cfd-415a6caad49a)
