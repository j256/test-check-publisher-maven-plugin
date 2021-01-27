Test Check Publisher
====================

This maven plugin is designed to take tests results produced by continuous integration and publish
them to github as a "check" annotation on a particular commit.

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

For the full instructions about how to get this working, see the
[Test Check Publisher Github application](https://github.com/apps/test-check-publisher).
