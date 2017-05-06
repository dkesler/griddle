package com.yodle.griddle

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.util.zip.ZipFile

class IdlPluginFunctionalTest extends Specification {
    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile
    public static final List<String> GRADLE_VERSIONS = ['2.8', '2.9', '2.10', '2.11', '2.12', '2.13', '2.14.1', '3.0', '3.1', '3.2', '3.3', '3.4', '3.5']

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    def ""() {
        given:
          buildFile << """
plugins {
  id 'idl'
  id 'java'
}

archivesBaseName = 'test-project'
                       """

        testProjectDir.newFolder("src", "main", "thrift", "SubDir")
        def idlFile = testProjectDir.newFile("src/main/thrift/SubDir/TestGenerateIdl.thrift")
        idlFile << """
namespace java com.yodle.griddle.test

service TestGenerateService {

}
"""

        when:
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments(['build'])
                .withGradleVersion(gradleVersion)
                .build()

        def idlJar = new ZipFile(new File(testProjectDir.root, "build/libs/test-project-idl.jar"))

        then:
        idlJar.entries().collect { it.name }.contains('SubDir/TestGenerateIdl.thrift')

        where:
        gradleVersion << GRADLE_VERSIONS
    }
}
