// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.webpack

import com.intellij.javascript.debugger.NodeJsAppRule
import com.intellij.lang.javascript.buildTools.webpack.WebpackIntegrationTestBase
import org.jetbrains.vuejs.lang.vueRelativeTestDataPath

class VueWebpackTest : WebpackIntegrationTestBase() {
  override fun getBasePath(): String = vueRelativeTestDataPath() + "/libraries/webpack"

  fun testNuxt() {
    doWebpackTest("module/src", "js")
  }

  fun testVueCli() {
    doWebpackTest("module/src", "js")
  }

  fun testVueCliWorkspace() {
    doWebpackTest("module", "js")
  }

  override fun configureInterpreterVersion(): NodeJsAppRule {
    return NodeJsAppRule.LATEST_16
  }
}