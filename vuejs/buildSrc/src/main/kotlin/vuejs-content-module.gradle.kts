// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Shared build logic for Vue content modules. Applied via `plugins { id("vuejs-content-module") }`.
// Each module supplies only what differs: source dirs (when not the flat default below),
// bundledPlugins/bundledModules, and project dependencies.
plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm")
  id("org.jetbrains.intellij.platform.module")
}

repositories {
  mavenCentral()
  intellijPlatform {
    defaultRepositories()
    snapshots()
  }
}

// Flat Vue layout; modules with generated sources override these dirs.
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
    intellijIdeaUltimate(ext("platform.version")) {
      useInstaller = false
    }
  }
}

kotlin {
  jvmToolchain(25)
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_25)
    @Suppress("UNCHECKED_CAST")
    freeCompilerArgs.addAll(rootProject.extensions["kotlin.freeCompilerArgs"] as List<String>)
  }
}

fun ext(name: String): String =
  rootProject.extensions[name] as? String
  ?: error("Property `$name` is not defined")
