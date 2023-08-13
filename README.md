### blankaspect/pathnameAssistant

This repository contains the source code and resources of the PathnameAssistant application.  The repo conforms to the
Maven standard directory layout \(ie, the source code is in `src/main/java` and the resources are in
`src/main/resources`\).

The Java version of the source code is 17.

The source files in this repo have an expected tab width of 4.

----

The Kotlin-DSL-based Gradle build script extends the `compileJava` and `jar` tasks of the `java` plug-in and defines
the following tasks for launching the application:
* `runMain` \(for use after `compileJava`\)\
Runs the main class with Gradle's Java launcher. 
* `runMainJre` \(for use after `compileJava`\)\
Runs the main class with a Java launcher from a JRE that includes JavaFX modules.
* `runJar` \(for use after `jar`\)\
Runs the JAR with Gradle's Java launcher.
* `runJarJre` \(for use after `jar`\)\
Runs the JAR with a Java launcher from a JRE that includes JavaFX modules.

PathnameAssistant is a non-modular JavaFX application.  The `compileJava`, `jar`, `runMain` and `runJar` tasks expect
the JavaFX modules to be provided by a [JavaFX SDK](https://gluonhq.com/products/javafx/); the two `run*Jre` tasks
expect the JavaFX modules to be provided by a JRE.

JavaFX modules are included in some third-party JREs, such as the
[Azul Zulu JRE FX packages](https://www.azul.com/downloads/).  If you prefer to create your own JRE that includes JavaFX
modules, the [blankaspect/makejre](https://github.com/blankaspect/makejre) repo contains scripts for Linux and Windows
that may be useful. 

----

The contents of this repository are covered by two licences:

* You may use the contents of the `uk.blankaspect.pathnameassistant` package \(including resources\) under the terms of
the GPL version 3 license.
* You may use all other contents of this repository under the terms of the MIT license.
