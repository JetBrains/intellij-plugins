// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.webpack

import com.intellij.testFramework.junit5.TestApplication
import com.intellij.webpack.webpackIntegrationFixture
import org.jetbrains.vuejs.lang.vueRelativeTestDataPath
import org.junit.jupiter.api.Test

@TestApplication
class VueWebpackTest {
  private val webpack by webpackIntegrationFixture(vueRelativeTestDataPath() + "/libraries/webpack")

  @Test
  fun testNuxt() {
    webpack.doWebpackTest("module/src", "js")
  }

  @Test
  fun testVueCli() {
    webpack.doWebpackTest("module/src", "js")
  }

  @Test
  fun testVueCliWorkspace() {
    webpack.doWebpackTest("module", "js")
  }
}
