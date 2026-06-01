// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
plugins {
  id("vuejs-content-module")
}

sourceSets {
  main {
    java {
      setSrcDirs(listOf("src", "gen"))
    }
  }
}

dependencies {
  intellijPlatform {
    bundledPlugins("JavaScript", "com.intellij.css")
  }
}
