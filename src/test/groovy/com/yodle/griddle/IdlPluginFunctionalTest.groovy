package com.yodle.griddle

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.util.zip.ZipFile

import static com.yodle.griddle.TestedVersions.GRADLE_VERSIONS

class IdlPluginFunctionalTest extends Specification {
    public static final String IDL_FILE_CONTENTS = """
namespace java com.yodle.griddle.test

service TestGenerateService {

}
"""
    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    def "idl jar and regular jar include idl"() {
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
        idlFile << IDL_FILE_CONTENTS
        when:
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments(['build'])
                .withGradleVersion(gradleVersion)
                .build()

        def idlJar = new ZipFile(new File(testProjectDir.root, "build/libs/test-project-idl.jar"))
        def standardJar = new ZipFile(new File(testProjectDir.root, "build/libs/test-project.jar"))

        then:
        idlJar.entries().collect { it.name }.contains('SubDir/TestGenerateIdl.thrift')
        standardJar.entries().collect { it.name }.contains('SubDir/TestGenerateIdl.thrift')

        where:
        gradleVersion << GRADLE_VERSIONS
    }

    def "thrift src dir can be overridden"() {
        given:
        buildFile << """
plugins {
  id 'idl'
  id 'java'
}

archivesBaseName = 'test-project'

thriftSrcDir = "\${projectDir}/src/main/thrift2"

                       """

        testProjectDir.newFolder("src", "main", "thrift2", "SubDir")
        def idlFile = testProjectDir.newFile("src/main/thrift2/SubDir/TestGenerateIdl.thrift")
        idlFile << IDL_FILE_CONTENTS
        when:
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments(['build'])
                .withGradleVersion(gradleVersion)
                .build()

        def idlJar = new ZipFile(new File(testProjectDir.root, "build/libs/test-project-idl.jar"))
        def standardJar = new ZipFile(new File(testProjectDir.root, "build/libs/test-project.jar"))

        then:
        idlJar.entries().collect { it.name }.contains('SubDir/TestGenerateIdl.thrift')
        standardJar.entries().collect { it.name }.contains('SubDir/TestGenerateIdl.thrift')

        where:
        gradleVersion << GRADLE_VERSIONS
    }

    def "thrift gen dir can be overridden"() {
        given:
        buildFile << """
plugins {
  id 'idl'
  id 'java'
}

archivesBaseName = 'test-project'

thriftGenDir = "\${buildDir}/gen-src2"

                       """

        testProjectDir.newFolder("src", "main", "thrift", "SubDir")
        def idlFile = testProjectDir.newFile("src/main/thrift/SubDir/TestGenerateIdl.thrift")
        idlFile << IDL_FILE_CONTENTS
        when:
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments(['build'])
                .withGradleVersion(gradleVersion)
                .build()

        def idlJar = new ZipFile(new File(testProjectDir.root, "build/libs/test-project-idl.jar"))
        def standardJar = new ZipFile(new File(testProjectDir.root, "build/libs/test-project.jar"))

        then:
        idlJar.entries().collect { it.name }.contains('SubDir/TestGenerateIdl.thrift')
        standardJar.entries().collect { it.name }.contains('SubDir/TestGenerateIdl.thrift')

        where:
        gradleVersion << GRADLE_VERSIONS
    }

    def "idl component created"() {
        given:
        buildFile << """
plugins {
  id 'idl'
  id 'java'
  id 'maven-publish'
}

archivesBaseName = 'test-project'

publishing {
    publications {
        idl(MavenPublication) {
            groupId='griddle-test'
            artifactId='test-project'
            version='0.1'
            from components.idl
        }
    }
    repositories {
        maven {
            url "\$buildDir/repo"
        }
    }
}
                       """

        testProjectDir.newFolder("src", "main", "thrift", "SubDir")
        def idlFile = testProjectDir.newFile("src/main/thrift/SubDir/TestGenerateIdl.thrift")
        idlFile << IDL_FILE_CONTENTS
        when:
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments(['build', 'publish'])
                .withGradleVersion(gradleVersion)
                .build()

        def idlJar = new ZipFile(new File(testProjectDir.root, "build/repo/griddle-test/test-project/0.1/test-project-0.1-idl.jar"))

        then:
        idlJar.entries().collect { it.name }.contains('SubDir/TestGenerateIdl.thrift')

        where:
        gradleVersion << GRADLE_VERSIONS
    }
}
