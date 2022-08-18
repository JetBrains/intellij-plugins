// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
buildscript {
  repositories {
    mavenCentral()
    maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
    maven("https://www.jetbrains.com/intellij-repository/snapshots")
  }

  pluginManagement {
    plugins {
      id("java")
      id("org.jetbrains.kotlin.jvm") version "1.7.10"
      id("org.jetbrains.intellij") version "1.8.0"
    }
  }
}
