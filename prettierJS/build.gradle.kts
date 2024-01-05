// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
apply(from = "../contrib-configuration/common.gradle.kts")

plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm")
  id("org.jetbrains.intellij")
}

intellij {
  pluginName.set("intellij.prettierJS")
  plugins.set(listOf("JavaScript"))

  version.set("LATEST-EAP-SNAPSHOT")
  type.set("IU")
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
      //setSrcDirs(listOf("test"))
    }
  }
}

dependencies {
  //testImplementation("com.jetbrains.intellij.javascript:javascript-test-framework:LATEST-EAP-SNAPSHOT")
}

tasks {
  prepareSandbox {
    from("gen") {
      include("**/*.js")
      into("prettier")
    }
  }
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