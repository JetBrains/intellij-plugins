// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.webSymbols.moveToOffsetBySignature
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule

class Angular2EditorTest : Angular2TestCase("editor"){

  fun testBlockTyping() =
    doConfiguredTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0,
                     fileContents = "<div><caret></div>", extension = "html", checkResult = true) {
      myFixture.type("@if (")
      myFixture.moveToOffsetBySignature("()<caret>")
      myFixture.type("{\n<div>")
    }
}