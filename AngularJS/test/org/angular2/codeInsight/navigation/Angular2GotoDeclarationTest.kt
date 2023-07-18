// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationOrUsageHandler2
import com.intellij.webSymbols.checkGTDUOutcome
import com.intellij.webSymbols.checkGotoDeclaration
import org.angular2.Angular2TestCase

class Angular2GotoDeclarationTest : Angular2TestCase("navigation/declaration") {

  fun testExportAs() = doTest(92)

  fun testExportAsHostDirectives() = doTest(186)

  fun testComponentStandardElementSelector() = doConfiguredTest {
    checkGTDUOutcome(GotoDeclarationOrUsageHandler2.GTDUOutcome.GTD)
  }

  fun testComponentStandardAttributeSelector() = doConfiguredTest {
    checkGTDUOutcome(GotoDeclarationOrUsageHandler2.GTDUOutcome.GTD)
  }

  private fun doTest(expectedOffset: Int) {
    doConfiguredTest {
      checkGotoDeclaration(expectedOffset)
    }
  }

}