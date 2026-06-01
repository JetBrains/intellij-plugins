// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
plugins {
  id("vuejs-content-module")
}

dependencies {
  intellijPlatform {
    bundledPlugins("org.intellij.plugins.markdown", "JavaScript")
  }

  implementation(project(":vuejs-backend"))
  implementation(project(":vuejs-common"))
}
