// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
    name.set("Serial Monitor")
  }
}

dependencies {
  intellijPlatform {
    jetbrainsRuntime()
    clion("LATEST-EAP-SNAPSHOT", useInstaller = false)
  }
  implementation("org.jetbrains.intellij.deps:jSerialComm:2.11.2")
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.fromTarget(ext("kotlin.jvmTarget")))
    @Suppress("UNCHECKED_CAST")
    freeCompilerArgs.addAll(rootProject.extensions["kotlin.freeCompilerArgs"] as List<String>)
    freeCompilerArgs.add("-Xwhen-guards")
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
  runIde {
     autoReload = false
  }
}

tasks {
  patchPluginXml {
    changeNotes.set("""
      <ul>
       <li>Original version by Dmitry Cherkas is forked</li>
       <li>Bunch of fixes</li>
       <li>Multiple serial connections are supported</li>
       <li>Apple M1 support</li>
      </ul>"""
    )
  }
}

fun ext(name: String): String =
  rootProject.extensions[name] as? String
  ?: error("Property `$name` is not defined")