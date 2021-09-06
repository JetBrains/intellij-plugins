// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
buildscript {
  repositories {
    mavenCentral()
  }
}

repositories {
  mavenCentral()
  maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
}

plugins {
  id("org.jetbrains.intellij") version "0.4.26"
  java
  kotlin("jvm") version "1.4.0"
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  testImplementation("junit", "junit", "4.12")
}

sourceSets {
  main {
    java {
      setSrcDirs(listOf("src", "gen"))
    }
    resources {
      setSrcDirs(listOf("resources"))
    }
  }
  test {
    java {
      setSrcDirs(listOf("test"))
    }
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

val ideVersion = "203-SNAPSHOT"

intellij {
  version = "IU-${ideVersion}"
  pluginName = "AngularJS"
  downloadSources = true
  updateSinceUntilBuild = false
  setPlugins("JavaScriptLanguage", "JSIntentionPowerPack", "CSS", "uml", "tslint")
}

dependencies {
  testImplementation("com.jetbrains.intellij.javascript:javascript-test-framework:${ideVersion}")
  testImplementation("com.jetbrains.intellij.resharper:resharper-test-framework:${ideVersion}")
  testImplementation("com.jetbrains.intellij.copyright:copyright:${ideVersion}")
  testImplementation("com.mscharhag.oleaster:oleaster-matcher:0.2.0")
  testImplementation("com.mscharhag.oleaster:oleaster-runner:0.2.0")
}

tasks {
  withType(JavaCompile::class.java) {
    options.encoding = "UTF-8"
  }
  withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java) {
    kotlinOptions.jvmTarget = "11"
    kotlinOptions.freeCompilerArgs = listOf("-Xjvm-default=compatibility")
  }
  test {
    systemProperty("idea.home.path", File("${projectDir}/../").absolutePath)
  }
  wrapper {
    gradleVersion = "6.6.1"
  }
}