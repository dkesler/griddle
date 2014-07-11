# Griddle

The Griddle project is a set of plugins that empower Gradle to understand how to manage and generate thrift files.  These plugins provide features such as

* Exporting the idl of a project so that any consumer can generate that idl in the language of its choice with the generator of its choice
* Allowing idl files in one project to `include` idl files that live in another gradle subproject, in another gradle project, or in any arbitrary jar
* Easy, portable generation using Twitter's [Scrooge](https://github.com/twitter/scrooge) generator or the default [Apache Thrift](https://thrift.apache.org/) generator

# Plugins

## The Idl Plugin

The `idl` plugin enables Gradle to understand and manage idl based dependencies and provides basic support for projects that want to generate thrift files.  It provides 

* Two new Configurations
* Three new tasks: `idlJar`, `copyDependencyIdl`, and `copyIncludedIdl`
* Options to control the directories containing thrift files or used by the thrift generation process

### Configurations

#### idl

The `idl` configuration indicates that the dependency (and/or one or more of its transitive dependencies) has exported thrift files in its jars, and those thrift files should be generated by a consuming project.  All `idl` dependencies are automatically added to the `compile` configuration

#### compiledIdl

The `compiledIdl` configuration, much like the `idl` configuration, indicates that the dependency and/or one or more of its transitive dependencies) has exported thrift files in its jars.  However unlike the `idl` configuration, those thrift files have already been generated.  This is used to indicate dependencies whose exported thrift files may be `include`ed by the dependent project.  All `compiledIdl` depenencies are added to the `compile` configuration.

### Tasks

#### idlJar

By default, all source thrift files of projects using the `idl` plugin will be included in the default jar produced by the project.  In the event that you want a jar containing _only_ the thrift files of the project, you may use the `idlJar` task.  It will create a jar with the classifier `idl` containing only the source thrift files of the project.

#### copyDependencyIdl and copyIncludedIdl

These tasks are responsible for copying idl from `idl` and `compiledIdl` dependencies respectively into a defined location so that generators can generate or include them.  You should never need to manually configure these tasks.  Instead, utilize the configuration options below.

### Configuration Options

The `idl` plugin adds four configuration options directly to each gradle project using the `idl` plugin.

#### thriftSrcDir

This option indicates the directory containing the source thrift files for the project.  It defaults to "src/main/thrift".  Any directory structure under thriftSrcDir will be preserved when the thrift files are exported in the project's jar files.

#### thriftGenDir

This option indicates the directory that generators should generate real source files into.  It defaults to "build/gen-src".

#### dependencyIdlDir

This option indicates a staging directory for all thrift files provided by `idl` dependencies.  I.e. thrift files that must be generated.  Defaults to "build/idl/dependency"

#### includedIdlDir

This option indicates a staging directory for all thrift files provided by `compiledIdl` dependencies.  I.e. thrift files that may be included by other thrift files but should not themselves be genrated.  Defaults to "build/idl/included"

## The Thrift Plugin

In addition to applying the `idl` plugin, the `thrift` plugin adds the capability to generate thrift files using the standard Apache Thrift generator in any language supported by that generator.  It will also integrate the interface generation process into the Java build process.

### Tasks

#### generateInterfaces

The `generateInterfaces` task will automatically generate all thrift files provided by `idl` dependencies or under the `thriftSrcDir` specified for the project.  By default, it will generate java classes using the `java:hashcode` generator.  This can be overridden by setting the `language` property on the `generateInterfaces` task to any language supported by the thrift generator you are using.  The `generateInterfaces` task assumes that there will be a thrift generator binary named `thrift` on the `PATH`.  If you would rather explicitly specify the location of the thrift binary, you can override the `generator` property of the `generateInterfaces` task. 

## The Scrooge and Scrooge-Java plugins

Much like the `thrift` plugin, the `scrooge` and `scrooge-java` plugins integrate idl generation into the scala or java build processes respectively, using Twitter's Scrooge generator instead.

### Configurations

#### scroogeGen

The scrooge plugins adds a `scroogeGen` configuration.  This should be used to specify the dependency on the desired version of the scrooge generator plugin.  This configuration is _not_ applied as a `compile` dependency, so you will need to additionally specify a `compile` dependency on the desired version of the scrooge runtime.

### Tasks

#### generateInterfaces

The `generateInterfaces` task will automatically generate all thrift files provided by `idl` dependencies or under the `thriftSrcDir` specified for the project.  The `scrooge` plugin will generate native scala interfaces while the `scrooge-java` plugin will generate java interfaces.  By default, it will generate [finagled](https://github.com/twitter/finagle) interfaces.  This can be disabled by setting the `useFinagle` property on the `generateInterfaces` task to false.


# Interaction With Idea Plugin

If you are using the [IntelliJ Idea](http://www.gradle.org/docs/current/userguide/idea_plugin.html) plugin alongside the `thrift`, `scrooge`, or `scrooge-java` plugins, there are a couple extra steps you will need to take to ensure that your project imports successfully.  By default, the the generator projects generate into a folder under the build directory, however this directory is automatically marked as excluded by the idea plugin and will not be added as a project directory.  To get around this, you will either need to move the `thriftGenDir` outside the build directory or modify the idea plugin's exclude settings via:

    idea.module {
        excludeDirs -= file(buildDir)
        excludeDirs += file("${buildDir}/classes") //Not strictly necessary, but nice for cleanliness
        sourceDirs += file("${thriftGenDir}/gen-java") //For the thrift plugin only
    }

excluding any other subdirectories of the build dir that you would like.  Additionally, if you are using the `thrift` plugin, you will need to compensate for the fact that the thrift generator will always generate to a subdirectory of the directory you point it to.  For example, if you are generating java interfaces, you will need to add '${thriftGenDir}/gen-java' as a source directory.

Lastly, you need to make sure that you generate interfaces _before_ running `gradle idea`.  Otherwise the `thriftGenDir` directory will not yet exist and IDEA will not add it as a source directory.


# Sample Usages