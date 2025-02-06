// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.webSymbols.testFramework.moveToOffsetBySignature
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule

class Angular2EditorTest : Angular2TestCase("editor", false) {

  fun testBlockTyping() =
    doConfiguredTest(Angular2TestModule.ANGULAR_CORE_17_3_0,
                     fileContents = "<div></div><!--within tag-->\n <!--space-->\ntext", extension = "html", checkResult = true) {
      moveToOffsetBySignature("<div><caret></div>")
      type("@if (")
      moveToOffsetBySignature("()<caret>")
      type("{\n<div>")
      moveToOffsetBySignature("<caret> <!--space-->")
      type("@if (")
      moveToOffsetBySignature("<caret>text")
      type("@if (")
    }

  fun testBlockFolding() {
    doFoldingTest(Angular2TestModule.ANGULAR_CORE_17_3_0,
                  extension = "html")
  }

  fun testClosingBraceWithInterpolation() =
    doConfiguredTest(Angular2TestModule.ANGULAR_CORE_17_3_0,
                     fileContents = "@if (foo) { {{foo}} } @else { <caret>", extension = "html", checkResult = true) {
      type("}")
    }

  fun testDeletingParenInParams() =
    doConfiguredTest(Angular2TestModule.ANGULAR_CORE_17_3_0,
                     fileContents = "@if (((<caret>))) { }", extension = "html", checkResult = true) {
      type("\b\b\b")
    }



}