// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.models.Coordinates
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
    name = "Perforce"
  }
}

sourceSets {
  main {
    java {
      setSrcDirs(listOf("src"))
    }
    resources {
      setSrcDirs(listOf("resources"))
    }
  }
  test {
    java {
      setSrcDirs(listOf("testSource"))
    }
    resources {
      setSrcDirs(listOf("testResources"))
    }
  }
}

dependencies {
  intellijPlatform {
    bundledModules("intellij.platform.vcs.impl")
    bundledPlugins("Git4Idea")
    jetbrainsRuntime()
    intellijIdeaUltimate(ext("platform.version"), useInstaller = false)
    platformDependency(Coordinates("com.jetbrains.intellij.platform", "vcs-test-framework"))
    testFramework(TestFrameworkType.Platform)
    testFramework(TestFrameworkType.Bundled)
  }

  implementation(project(":perforce_util"))

  testImplementation("junit:junit:${ext("junit.version")}")
  testImplementation("org.mockito:mockito-core:5.19.0")
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
    gradleVersion = ext("gradle.version")
  }
}

fun ext(name: String): String =
  rootProject.extensions[name] as? String
  ?: error("Property `$name` is not defined")
