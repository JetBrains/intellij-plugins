// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.models.Coordinates

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
    name = "Vue.js"
  }
}

dependencies {
  intellijPlatform {
    jetbrainsRuntime()
    intellijIdeaUltimate(ext("platform.version")) {
      useInstaller = false
    }
    // Bundled IDEA plugins on the test/sandbox classpath. JavaScript and postcss back the Vue
    // plugin itself; the rest are only exercised by vuejs-tests.
    bundledPlugins(
      "JavaScript",
      "org.intellij.plugins.postcss",
      "com.intellij.css",
      "org.jetbrains.plugins.sass",
      "org.jetbrains.plugins.less",
      "HtmlTools",
      "com.intellij.copyright",
      "intellij.webpack",
      "JSIntentionPowerPack",
      "JavaScriptDebugger",
    )

    pluginModule(implementation(project(":vuejs-common")))
    pluginModule(implementation(project(":vue-free")))
    pluginModule(implementation(project(":vuejs-backend")))
    pluginModule(implementation(project(":vuejs-webpack")))
    pluginModule(implementation(project(":vuejs-debugger")))
    pluginModule(implementation(project(":vuejs-markdown")))
    pluginModule(implementation(project(":vuejs-prettier")))
    pluginModule(implementation(project(":vuejs-copyright")))
    pluginModule(implementation(project(":vuejs-eslint")))

    // Marketplace-only plugins used by some tests (not bundled in IDEA Ultimate); set a version to enable:
    // plugin("org.jetbrains.plugins.stylus", "<version>")
    // plugin("com.jetbrains.plugins.Jade", "<version>")
    testFramework(TestFrameworkType.Platform)
    testFramework(TestFrameworkType.Plugin.XML)
    testFramework(TestFrameworkType.Plugin.JavaScript)
    platformDependency(Coordinates("com.jetbrains.intellij.platform", "poly-symbols-test-framework"))
    platformDependency(Coordinates("com.jetbrains.intellij.platform", "lsp-test-framework"))
  }

  testImplementation("junit:junit:${ext("junit.version")}")
}

kotlin {
  jvmToolchain(25)
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_25)
    @Suppress("UNCHECKED_CAST")
    freeCompilerArgs.addAll(rootProject.extensions["kotlin.freeCompilerArgs"] as List<String>)
  }
}

tasks {
  wrapper {
    // Vue-local override of the shared gradle.version (8.14.3): plugin 2.16.0 requires Gradle 9.0+.
    gradleVersion = "9.5.1"
  }
}

sourceSets {
  main {
    resources {
      setSrcDirs(listOf("vuejs-plugin/resources"))
    }
  }
  test {
    java {
      setSrcDirs(listOf("vuejs-tests/src", "vuejs-tests/src-ext"))
    }
    resources {
      setSrcDirs(listOf("vuejs-tests/testData"))
    }
  }
}

fun ext(name: String): String =
  rootProject.extensions[name] as? String
  ?: error("Property `$name` is not defined")