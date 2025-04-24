// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.extensions.IntelliJPlatformDependenciesExtension
import org.jetbrains.intellij.pluginRepository.PluginRepositoryFactory
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
    name = "Jade"
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
      setExcludes(listOf("com/jetbrains/plugins/jade/watcher/**"))
    }
    resources {
      setSrcDirs(listOf("testData", "testResources"))
    }
  }
}

dependencies {
  intellijPlatform {
    bundledPlugins("JavaScript", "JSIntentionPowerPack", "HtmlTools", "com.intellij.css")
    pluginsInLatestCompatibleVersion(
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
  kotlinDaemonJvmArgs = listOf("-Xmx1024m")
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

private val IntelliJPlatformDependenciesExtension.pluginRepository by lazy {
  PluginRepositoryFactory.create("https://plugins.jetbrains.com")
}

fun IntelliJPlatformDependenciesExtension.pluginsInLatestCompatibleVersion(vararg pluginIds: String) =
  plugins(provider {
    pluginIds.map { pluginId ->
      val platformType = intellijPlatform.productInfo.productCode
      val platformVersion = intellijPlatform.productInfo.buildNumber

      val plugin = pluginRepository.pluginManager.searchCompatibleUpdates(
        build = "$platformType-$platformVersion",
        xmlIds = listOf(pluginId),
      ).firstOrNull()
        ?: throw GradleException("No plugin update with id='$pluginId' compatible with '$platformType-$platformVersion' found in JetBrains Marketplace")

      "${plugin.pluginXmlId}:${plugin.version}"
    }
  })

fun ext(name: String): String =
  rootProject.extensions[name] as? String
    ?: error("Property `$name` is not defined")