// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
apply(from = "../contrib-configuration/common.gradle.kts")

plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm")
  id("org.jetbrains.intellij")
}

intellij {
  pluginName.set("Yeoman")
  plugins.set(listOf("JavaScript"))

  version.set("LATEST-EAP-SNAPSHOT")
  type.set("IU")
}

sourceSets {
  main {
    java {
      setSrcDirs(listOf("src"))
    }
    resources {
      setSrcDirs(listOf("resources", "compatibilityResources"))
    }
  }
  test {
    java {
      setSrcDirs(listOf("testSrc"))
    }
  }
}

tasks {
  prepareSandbox {
    from("resources") {
      include("runner/**/*.js")
      into("Yeoman")
    }
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

fun ext(name: String): String =
  rootProject.extensions[name] as? String
  ?: error("Property `$name` is not defined")