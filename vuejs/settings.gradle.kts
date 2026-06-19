// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
  plugins {
    id("java")

    // remove when common settings bumped
    id("org.jetbrains.kotlin.jvm") version "2.3.21"
    id("org.jetbrains.intellij.platform") version "2.16.0"
    id("org.jetbrains.intellij.platform.settings") version "2.16.0"
  }
}

include(":vuejs-common")
include(":vue-free")
include(":vuejs-backend")
include(":vuejs-webpack")
include(":vuejs-debugger")
include(":vuejs-markdown")
include(":vuejs-prettier")
include(":vuejs-copyright")
include(":vuejs-eslint")