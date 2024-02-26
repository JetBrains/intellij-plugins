// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
apply(from = "../contrib-configuration/common.gradle.kts")

plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm")
  id("org.jetbrains.intellij")
}

intellij {
  pluginName.set("Serial Monitor")
  version.set("LATEST-EAP-SNAPSHOT")
  type.set("IU")
}

dependencies {
  implementation ("io.github.java-native:jssc:2.9.4")  {
    exclude("org.slf4j", "slf4j-api")
  }
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = ext("kotlin.jvmTarget")
    @Suppress("UNCHECKED_CAST")
    kotlinOptions.freeCompilerArgs = rootProject.extensions["kotlin.freeCompilerArgs"] as List<String>
  }
  java {
    sourceCompatibility = JavaVersion.toVersion(ext("java.sourceCompatibility"))
    targetCompatibility = JavaVersion.toVersion(ext("java.targetCompatibility"))
  }
  wrapper {
    gradleVersion = "8.5"
  }
  runIde {
    autoReloadPlugins.set(false)
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