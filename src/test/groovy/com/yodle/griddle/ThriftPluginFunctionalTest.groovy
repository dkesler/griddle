package com.yodle.griddle

import groovy.io.FileType
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class ThriftPluginFunctionalTest extends Specification {
    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile
    public static final List<String> GRADLE_VERSIONS = ['2.8', '2.9', '2.10', '2.11', '2.12', '2.13', '2.14.1', '3.0', '3.1', '3.2', '3.3', '3.4', '3.5']

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    def "test thrift generation"() {
        given:
          buildFile << """
plugins {
 id 'thrift'
}

repositories {
  mavenCentral()
}

dependencies {
  compile "org.apache.thrift:libthrift:0.10.0"
}
                       """

        testProjectDir.newFolder("src", "main", "thrift", "SubDir")
        def idlFile1 = testProjectDir.newFile("src/main/thrift/SubDir/TestGenerateIdl.thrift")
        idlFile1 << """
namespace java com.yodle.griddle.test

service TestGenerateService {
}
"""

        def idlFile2 = testProjectDir.newFile("src/main/thrift/SubDir/TestGenerateIdl2.thrift")
        idlFile2 << """
namespace java com.yodle.griddle.test

service TestGenerateService2 {
}
"""

        when:
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments(['build'])
                .withGradleVersion(gradleVersion)
                .build()

        def generatedSourceFiles = getFiles(new File(testProjectDir.root, "build/gen-src"))
        def generatedClasses = getFiles(new File(testProjectDir.root, "build/classes"))

        then:
        generatedSourceFiles == ['TestGenerateService.java', 'TestGenerateService2.java'] as Set
        generatedClasses.containsAll(['TestGenerateService.class', 'TestGenerateService2.class'])

        where:
        gradleVersion << GRADLE_VERSIONS
    }

    Set<String> getFiles(File file) {
        Set <String> files = [] as Set
        file.eachFileRecurse(FileType.FILES) {
            files << it.name
        }
        return files
    }
}
