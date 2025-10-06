// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
buildscript {
  repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    gradlePluginPortal()
  }

  pluginManagement {
    repositories {
      maven("https://oss.sonatype.org/content/repositories/snapshots/")
      gradlePluginPortal()
    }
    plugins {
      id("java")
      id("org.jetbrains.kotlin.jvm") version "2.2.20"
      id("org.jetbrains.intellij.platform") version "2.9.0"
      id("org.jetbrains.intellij.platform.settings") version "2.9.0"
    }
  }
}