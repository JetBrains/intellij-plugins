// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.javascript.debugger.NodeJsAppRule
import com.intellij.javascript.debugger.NodeJsAppRule.Companion.LATEST_22

class ReformatWithPrettierTypeScriptConfigStripTypeTest : ReformatWithPrettierBaseTest() {
  override fun configureInterpreterVersion(): NodeJsAppRule {
    return LATEST_22
  }

  fun testTypeScriptConfig() {
    doReformatFile("ts")
  }
}
