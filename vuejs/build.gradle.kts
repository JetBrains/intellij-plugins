// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
apply(from = "../contrib-configuration/common.gradle.kts")

plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm")
  id("org.jetbrains.intellij")
}

intellij {
  version.set("LATEST-EAP-SNAPSHOT")
  type.set("IU")
  pluginName.set("Vue.js")
  downloadSources.set(true)
  plugins.set(listOf("JavaScript", "JSIntentionPowerPack", "JavaScriptDebugger", "com.intellij.css", "HtmlTools",
                     "org.jetbrains.plugins.sass", "org.jetbrains.plugins.less", "intellij.webpack",
                     // Needed for tests-only
                     //"org.jetbrains.plugins.stylus:233.11799.172",
                     "org.intellij.plugins.postcss",
                     // Needed for tests-only
                     //"com.jetbrains.plugins.Jade:$targetVersion",
                     "intellij.prettierJS"
                     ))
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
    gradleVersion = ext("gradle.version")
  }
  runIde {
    autoReloadPlugins.set(false)
  }
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
      //setSrcDirs(listOf("vuejs-tests/src"))
    }
  }
}

dependencies {
  //testImplementation("com.jetbrains.intellij.javascript:javascript-test-framework:LATEST-EAP-SNAPSHOT")
}

fun ext(name: String): String =
  rootProject.extensions[name] as? String
  ?: error("Property `$name` is not defined")