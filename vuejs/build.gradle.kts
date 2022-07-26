// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
plugins {
  // Java support
  id("java")
  // Kotlin support
  id("org.jetbrains.kotlin.jvm") version "1.7.10"
  // Gradle IntelliJ Plugin
  id("org.jetbrains.intellij") version "1.5.2"
}

repositories {
  mavenCentral()
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
      setSrcDirs(listOf("vuejs-tests/src"))
    }
  }
}

val ideVersion = "LATEST-EAP-SNAPSHOT"
val buildVersion = "222.3345.108"

kotlin {
  jvmToolchain {
    this.languageVersion.set(JavaLanguageVersion.of(11))
  }
}

intellij {
  version.set("IU-${ideVersion}")
  pluginName.set("Vue.js")
  downloadSources.set(true)
  updateSinceUntilBuild.set(false)

  plugins.set(listOf("JavaScriptLanguage", "JSIntentionPowerPack", "JavaScriptDebugger", "CSS", "HtmlTools",
                      "org.jetbrains.plugins.sass", "org.jetbrains.plugins.less", "org.jetbrains.plugins.stylus",
                      "org.intellij.plugins.postcss:${buildVersion}",
                      "com.jetbrains.plugins.Jade:${buildVersion}",
                      "intellij.prettierJS:${buildVersion}"))
}