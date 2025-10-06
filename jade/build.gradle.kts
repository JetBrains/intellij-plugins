// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
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
    name = "Pug (ex-Jade)"
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
      setSrcDirs(listOf("tests"))
      setExcludes(listOf(
        "com/jetbrains/plugins/jade/watcher/**",
        "com/jetbrains/plugins/jade/parser/JadeParsingWithoutErrorsSuite.*",
        "com/jetbrains/plugins/jade/JadeInjectionTest.*"
      ))
    }
    resources {
      setSrcDirs(listOf("testData", "testResources"))
    }
  }
}

dependencies {
  intellijPlatform {
    bundledPlugins("JavaScript", "JSIntentionPowerPack", "HtmlTools", "com.intellij.css", "tanvd.grazi")
    compatiblePlugins(
      "com.intellij.plugins.watcher",
      "com.intellij.plugins.html.instantEditing",
      "org.coffeescript"
    )
    jetbrainsRuntime()
    intellijIdeaUltimate(ext("platform.version"), useInstaller = false)
    testFramework(TestFrameworkType.Platform)
    testFramework(TestFrameworkType.Plugin.JavaScript)
    testFramework(TestFrameworkType.Plugin.XML)
  }
  testImplementation("junit:junit:${ext("junit.version")}")
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.fromTarget(ext("kotlin.jvmTarget")))
    @Suppress("UNCHECKED_CAST")
    freeCompilerArgs.addAll(rootProject.extensions["kotlin.freeCompilerArgs"] as List<String>)
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
}

fun ext(name: String): String =
  rootProject.extensions[name] as? String
    ?: error("Property `$name` is not defined")