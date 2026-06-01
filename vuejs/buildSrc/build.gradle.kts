// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
plugins {
  `kotlin-dsl`
}

repositories {
  gradlePluginPortal()
  mavenCentral()
}

dependencies {
  // Put the plugins applied by the convention script(s) on the classpath so they compile and
  // expose their type-safe accessors (intellijPlatform { }, kotlin { }). Keep the versions in
  // sync with ../settings.gradle.kts pluginManagement.
  implementation("org.jetbrains.intellij.platform:intellij-platform-gradle-plugin:2.16.0")
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.21")
}
