// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.webSymbols.checkLookupItems
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule

class Angular2CompletionAutoPopupTest : Angular2TestCase("completionAutoPopup") {

  fun testForBlockTyping1() =
    doCompletionAutoPopupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0) {
      type("item ")

      // "of" keyword completion popup should show
      assertLookupShown()
      type("\n")

      // vars completion should show
      assertLookupShown()
      type("item\n; ")

      // "track" keyword completion popup should show
      assertLookupShown()
      type("\nit")

      // vars completion popup should show
      assertLookupShown()
      type("\n.")

      // properties completion popup should show
      assertLookupShown()
      type("na\n; ")

      // "let" keyword completion popup should show
      assertLookupShown()
      type("\nindex ")

      // "=" completion popup should show
      assertLookupShown()
      type("\n")

      // implicit vars completion popup should show
      assertLookupShown()
      type("\$in\n")
    }

  fun testForBlockTyping2() =
    doCompletionAutoPopupTest(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0) {
      type("item ")

      // "of" keyword completion popup should show
      assertLookupShown()
      type("of ")

      // vars completion should show
      assertLookupShown()
      type("items; ")

      // "track" keyword completion popup should show
      assertLookupShown()
      type("track ")

      // vars completion popup should show
      assertLookupShown()
      type("item.name; ")

      // "let" keyword completion popup should show
      assertLookupShown()
      type("let index ")

      // "=" completion popup should show
      assertLookupShown()
      type("= ")

      // implicit vars completion popup should show
      assertLookupShown()
    }

  fun testCompletionInExpression() {
    doCompletionAutoPopupTest(
      Angular2TestModule.ANGULAR_CORE_13_3_5, Angular2TestModule.ANGULAR_CDK_14_2_0, dir = true,
      before = {
        checkLookupItems(renderPriority = true, renderTypeText = true)
      }
    ) {
      // Export from other file
      type("kThemes\n")
      type(".")
      assertLookupShown()
      type("l\n;")

      // Local symbol
      type("CdkColors")
      completeBasic()
      type(".")
      assertLookupShown()
      type("re\n;")

      // Global symbol
      type("Ma")
      completeBasic()
      type("th\n")
      type(".")
      assertLookupShown()
      type("ab\n")
    }
  }
}