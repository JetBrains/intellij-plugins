// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
buildscript {
  repositories {
    mavenCentral()
  }
}

repositories {
  mavenCentral()
  maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
  maven("https://cache-redirector.jetbrains.com/www.myget.org/F/rd-snapshots/maven")
}

plugins {
  id("org.jetbrains.intellij") version "0.5.0"
  java
  kotlin("jvm") version "1.4.0"
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  testImplementation("junit", "junit", "4.12")
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

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

val ideVersion = "203-SNAPSHOT"

intellij {
  version = "IU-${ideVersion}"
  pluginName = "Vue.js"
  downloadSources = true
  updateSinceUntilBuild = false
  pluginsRepo {
    custom("https://buildserver.labs.intellij.net/guestAuth/repository/download/ijplatform_master_Idea_Installers/${buildVersion}/IU-plugins/plugins.xml")
  }

  setPlugins("JavaScriptLanguage", "JSIntentionPowerPack", "JavaScriptDebugger", "CSS", "HtmlTools",
             "org.jetbrains.plugins.sass", "org.jetbrains.plugins.less", "org.jetbrains.plugins.stylus",
             "org.intellij.plugins.postcss:${buildVersion}",
             "com.jetbrains.plugins.Jade:${buildVersion}",
             "intellij.prettierJS:${buildVersion}")
}

dependencies {
  testImplementation("com.jetbrains.intellij.javascript:javascript-test-framework:${ideVersion}")
  testImplementation("com.jetbrains.intellij.copyright:copyright:${ideVersion}")
}

tasks {
  withType(JavaCompile::class.java) {
    options.encoding = "UTF-8"
  }
  withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java) {
    kotlinOptions.jvmTarget = "11"
    kotlinOptions.freeCompilerArgs = listOf("-Xjvm-default=compatibility")
  }
  test {
    systemProperty("idea.home.path", File("${projectDir}/../").absolutePath)
  }
  wrapper {
    gradleVersion = "6.6.1"
  }
}