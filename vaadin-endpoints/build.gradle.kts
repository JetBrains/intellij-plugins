// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
  id("java")
  id("org.jetbrains.intellij.platform") version "2.1.0"
}

group = "org.strangeway.vaadin"
version = "251.0.0"

repositories {
  mavenCentral()
  intellijPlatform {
    defaultRepositories()
  }
}

dependencies {
  intellijPlatform {
    intellijIdeaUltimate("LATEST-EAP-SNAPSHOT", useInstaller = false)
    testFramework(TestFrameworkType.Platform)
    testFramework(TestFrameworkType.Bundled)
    bundledPlugin("com.intellij.java")
    bundledPlugin("com.intellij.properties")
    bundledPlugin("com.intellij.microservices.jvm")
    instrumentationTools()
  }
  testImplementation("junit:junit:4.13.2")
}

java.sourceSets["main"].java {
  srcDir("gen")
  srcDir("src")
}

java.sourceSets["main"].resources {
  srcDir("resources")
}

intellijPlatform {
  pluginConfiguration {
    ideaVersion {
      sinceBuild = "243"
      untilBuild = "252.*"
    }

    changeNotes = ""
  }
}

tasks {
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "21"
    targetCompatibility = "21"
  }
}
