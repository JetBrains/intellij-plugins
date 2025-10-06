// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm")
  id("org.jetbrains.intellij.platform")
}

intellijPlatform {
  buildSearchableOptions = false
}

repositories {
  mavenCentral()
  intellijPlatform {
    defaultRepositories()
    snapshots()
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
}

dependencies {
  intellijPlatform {
    jetbrainsRuntime()
    intellijIdeaUltimate(ext("platform.version"), useInstaller = false)
  }
}

tasks {
  java {
    sourceCompatibility = JavaVersion.toVersion(ext("java.sourceCompatibility"))
    targetCompatibility = JavaVersion.toVersion(ext("java.targetCompatibility"))
  }
  runIde {
     autoReload = false
  }
}

fun ext(name: String): String =
  rootProject.extensions[name] as? String
  ?: error("Property `$name` is not defined")
