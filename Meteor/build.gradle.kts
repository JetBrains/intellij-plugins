// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
apply(from = "../contrib-configuration/common.gradle.kts")

plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm")
  id("org.jetbrains.intellij")
}

val targetVersion = rootProject.extensions.get("targetVersion")

intellij {
  pluginName.set("Meteor")
  plugins.set(listOf("JavaScript", "JavaScriptDebugger",
                     "com.dmarcotte.handlebars:$targetVersion",
                     "com.intellij.plugins.html.instantEditing:$targetVersion",
                     "com.intellij.plugins.watcher:$targetVersion"))

  version.set("LATEST-EAP-SNAPSHOT")
  type.set("IU")
}

sourceSets {
  main {
    java {
      setSrcDirs(listOf("src", "gen"))
    }
    resources {
      setSrcDirs(listOf("resources", "compatibilityResources"))
    }
  }
  test {
    java {
      //setSrcDirs(listOf("testSrc"))
    }
  }
}

dependencies {
  //testImplementation("com.jetbrains.intellij.javascript:javascript-test-framework:LATEST-EAP-SNAPSHOT")
}