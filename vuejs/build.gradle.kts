// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
apply(from = "../contrib-configuration/common.gradle.kts")

plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm")
  id("org.jetbrains.intellij")
}

val targetVersion = rootProject.extensions["targetVersion"]
val iuVersion = rootProject.extensions["iuVersion"]

intellij {
  version.set(iuVersion.toString())
  type.set("IU")
  pluginName.set("Vue.js")
  downloadSources.set(true)
  plugins.set(listOf("JavaScript", "JSIntentionPowerPack", "JavaScriptDebugger", "com.intellij.css", "HtmlTools",
                     "org.jetbrains.plugins.sass", "org.jetbrains.plugins.less", "intellij.webpack",
                     "org.jetbrains.plugins.stylus:$targetVersion",
                     "org.intellij.plugins.postcss",
                     "com.jetbrains.plugins.Jade:$targetVersion",
                     "intellij.prettierJS"
                     ))
}

tasks {
  withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java) {
    kotlinOptions.jvmTarget = "17"
    kotlinOptions.freeCompilerArgs = listOf("-Xjvm-default=all")
  }
  java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
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