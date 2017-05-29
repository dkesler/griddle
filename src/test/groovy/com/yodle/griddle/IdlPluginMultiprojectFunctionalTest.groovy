package com.yodle.griddle

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.util.zip.ZipFile

import static com.yodle.griddle.TestedVersions.GRADLE_VERSIONS

class IdlPluginMultiprojectFunctionalTest extends Specification {
    public static final String IDL_FILE_CONTENTS = """
namespace java com.yodle.griddle.test

service TestGenerateService {

}
"""
    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()
    File dependentBuildFile
    File dependent2BuildFile

    def setup() {
        testProjectDir.newFile('build.gradle') << """
subprojects {
  group = 'com.yodle.griddle.test'
  version = '0.1'
}
"""

        testProjectDir.newFolder("dependent")
        dependentBuildFile = testProjectDir.newFile('dependent/build.gradle')

        testProjectDir.newFolder("dependent2")
        dependent2BuildFile = testProjectDir.newFile('dependent2/build.gradle')


        testProjectDir.newFolder("dependency");
        testProjectDir.newFile('dependency/build.gradle') << """
plugins {
  id 'idl'
  id 'java'
  id 'maven-publish'
}

publishing {
    publications {
        idl(MavenPublication) {
            from components.idl
        }
        mavenJava(MavenPublication) {
            from components.java
        }
    }
    repositories {
        maven {
            url "\${rootProject.projectDir}/build/repo"
        }
    }
}

publish.dependsOn copyDependencyIdl
"""
        testProjectDir.newFolder('dependency', 'src', 'main', 'thrift', 'SubDir')
        testProjectDir.newFile('dependency/src/main/thrift/SubDir/TestGenerateIdl.thrift') << """
namespace java com.yodle.griddle.test

service TestGenerateService {

}
"""

        testProjectDir.newFile('settings.gradle') << """
include 'dependency', 'dependent', 'dependent2'
"""
    }

    def "idl dependency copied  for thrift generation but not included in jar"() {
        given:
        dependentBuildFile << """
plugins {
  id 'idl'
  id 'java'
}

dependencies {
  idl project(':dependency')
}
                       """

        when:
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments(['copyDependencyIdl', 'build'])
                .withGradleVersion(gradleVersion)
                .build()

        def idlJar = new ZipFile(new File(testProjectDir.root, "dependent/build/libs/dependent-0.1-idl.jar"))

        then:
        !idlJar.entries().collect { it.name }.contains('SubDir/TestGenerateIdl.thrift')
        new File(testProjectDir.root, "dependent/build/idl/dependency/SubDir/TestGenerateIdl.thrift").exists()

        where:
        gradleVersion << GRADLE_VERSIONS
    }

    def "compiledIdl dependency copied for thirft inclusion but not included in jar"() {
        given:
        dependentBuildFile << """
plugins {
  id 'idl'
  id 'java'
}

dependencies {
  compiledIdl project(':dependency')
}
                       """

        when:
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments(['copyIncludedIdl', 'build'])
                .withGradleVersion(gradleVersion)
                .build()

        def idlJar = new ZipFile(new File(testProjectDir.root, "dependent/build/libs/dependent-0.1-idl.jar"))

        then:
        !idlJar.entries().collect { it.name }.contains('SubDir/TestGenerateIdl.thrift')
        new File(testProjectDir.root, "dependent/build/idl/included/SubDir/TestGenerateIdl.thrift").exists()

        where:
        gradleVersion << GRADLE_VERSIONS
    }

    def "idl dependency provided in pom produced by maven-publish"() {
        given:
        dependentBuildFile << """
plugins {
  id 'idl'
  id 'java'
  id 'maven-publish'
}

dependencies {
  idl project(':dependency')
}

publishing {
    publications {
        idl(MavenPublication) {
            from components.idl
        }
    }
    repositories {
        maven {
            url "\${rootProject.projectDir}/build/repo"
        }
    }
}
publish.dependsOn copyDependencyIdl
                       """


        dependent2BuildFile << """
plugins {
  id 'idl'
  id 'java'
}

repositories {
  maven {
    url "\${rootProject.projectDir}/build/repo"
  }
}

dependencies {
  idl "com.yodle.griddle.test:dependent:0.1"
}
"""

        when:
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments([':dependency:publish', ':dependent:publish', ':dependent2:copyDependencyIdl'])
                .withGradleVersion(gradleVersion)
                .build()

        then:
        new File(testProjectDir.root, "dependent2/build/idl/dependency/SubDir/TestGenerateIdl.thrift").exists()

        where:
        gradleVersion << GRADLE_VERSIONS
    }

    def "idl dependency provided in pom produced by maven"() {
        given:
        dependentBuildFile << """
plugins {
  id 'idl'
  id 'java'
  id 'maven'
}

dependencies {
  idl project(':dependency')
}

uploadArchives {
    repositories {
        mavenDeployer {
            repository(url: "file://\${rootProject.projectDir}/build/repo")
        }
    }
}

uploadArchives.dependsOn copyDependencyIdl
                       """

        dependent2BuildFile << """
plugins {
  id 'idl'
  id 'java'
}

repositories {
  maven {
    url "\${rootProject.projectDir}/build/repo"
  }
}

dependencies {
  idl "com.yodle.griddle.test:dependent:0.1"
}
"""

        when:
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments([':dependency:publish', ':dependent:uploadArchives', ':dependent2:copyDependencyIdl'])
                .withGradleVersion(gradleVersion)
                .build()

        then:
        new File(testProjectDir.root, "dependent2/build/idl/dependency/SubDir/TestGenerateIdl.thrift").exists()

        where:
        gradleVersion << GRADLE_VERSIONS
    }
}
