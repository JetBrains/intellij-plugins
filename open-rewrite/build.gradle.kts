// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

apply(from = "../contrib-configuration/common.gradle.kts")

plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm")
  id("org.jetbrains.intellij.platform")
}

repositories {
  intellijPlatform {
    defaultRepositories()
    snapshots()
  }
}

intellijPlatform {
  pluginConfiguration {
    name = "OpenRewrite"
  }
}

// The internal build splits this plugin across several JPS modules (core plus the gradle, maven,
// micronaut, quarkus and spring-boot integration content modules). For the standalone Gradle build
// we compile all of their source roots together as a single flat project, mirroring how other
// multi-module contrib plugins (e.g. Angular) are built outside the monorepo.
sourceSets {
  main {
    java {
      setSrcDirs(listOf(
        "src", "gen",
        "gradle/src",
        "maven/src",
        "micronaut/src",
        "quarkus/src",
        "spring-boot/src",
      ))
    }
    resources {
      setSrcDirs(listOf(
        "resources",
        "gradle/resources",
        "maven/resources",
        "micronaut/resources",
        "quarkus/resources",
        "spring-boot/resources",
      ))
    }
  }
  test {
    java {
      setSrcDirs(listOf(
        "test",
        "gradle/test",
        "maven/test",
      ))
    }
  }
}

dependencies {
  intellijPlatform {
    //intellijIdea(ext("platform.version")) {
    //  useInstaller.set(false)
    //}
    intellijIdeaUltimate(ext("platform.version")) {
      useInstaller.set(false)
    }
    jetbrainsRuntime()

    bundledModules("intellij.platform.lvcs.impl")

    bundledPlugins(
      "com.intellij.java",
      "com.intellij.gradle",
      "org.jetbrains.idea.maven",
      "org.jetbrains.plugins.yaml",
      "com.intellij.modules.json",
      "com.intellij.microservices.jvm",
      "com.intellij.micronaut",
      "com.intellij.quarkus",
      "com.intellij.spring.boot",
    )

    testFramework(TestFrameworkType.Platform)
    testFramework(TestFrameworkType.Plugin.Java)
  }
  testImplementation("junit:junit:${ext("junit.version")}")
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.fromTarget(ext("kotlin.jvmTarget")))
    @Suppress("UNCHECKED_CAST")
    freeCompilerArgs.addAll(rootProject.extensions["kotlin.freeCompilerArgs"] as List<String>)
  }
}

tasks {
  java {
    sourceCompatibility = JavaVersion.toVersion(ext("java.sourceCompatibility"))
    targetCompatibility = JavaVersion.toVersion(ext("java.targetCompatibility"))
  }
  wrapper {
    // Local override of the shared gradle.version (8.14.3): plugin 2.16.0 requires Gradle 9.0+.
    gradleVersion = "9.5.1"
  }
}

fun ext(name: String): String =
  rootProject.extensions[name] as? String
  ?: error("Property `$name` is not defined")
