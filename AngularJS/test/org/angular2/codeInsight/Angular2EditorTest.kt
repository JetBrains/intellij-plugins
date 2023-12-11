// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.webSymbols.moveToOffsetBySignature
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule

class Angular2EditorTest : Angular2TestCase("editor") {

  fun testBlockTyping() =
    doConfiguredTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0,
                     fileContents = "<div><caret></div>", extension = "html", checkResult = true) {
      type("@if (")
      moveToOffsetBySignature("()<caret>")
      type("{\n<div>")
    }

  fun testBlockFolding() {
    doFoldingTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0,
                  extension = "html")
  }

  fun testClosingBraceWithInterpolation() =
    doConfiguredTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0,
                     fileContents = "@if (foo) { {{foo}} } @else { <caret>", extension = "html", checkResult = true) {
      type("}")
    }

  fun testDeletingParenInParams() =
    doConfiguredTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0,
                     fileContents = "@if (((<caret>))) { }", extension = "html", checkResult = true) {
      type("\b\b\b")
    }

}