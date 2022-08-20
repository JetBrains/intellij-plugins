// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
apply(from = "../contrib-configuration/common.gradle.kts")

plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm")
  id("org.jetbrains.intellij")
}


intellij {
  pluginName.set("AngularJS")
  plugins.set(listOf("JavaScriptLanguage", "JSIntentionPowerPack", "CSS", "uml", "tslint", "intellij.webpack"))

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
  //testImplementation("com.jetbrains.intellij.resharper:resharper-test-framework:LATEST-EAP-SNAPSHOT")
  //testImplementation("com.jetbrains.intellij.copyright:copyright:LATEST-EAP-SNAPSHOT")
  testImplementation("com.mscharhag.oleaster:oleaster-matcher:0.2.0")
  testImplementation("com.mscharhag.oleaster:oleaster-runner:0.2.0")
}