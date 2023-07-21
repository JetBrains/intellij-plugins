// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationOrUsageHandler2
import com.intellij.webSymbols.checkGTDUOutcome
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule

class Angular2GotoDeclarationTest : Angular2TestCase("navigation/declaration") {

  fun testExportAs() = checkGotoDeclaration(92)

  fun testExportAsHostDirectives() = checkGotoDeclaration(186)

  fun testComponentStandardElementSelector() = doConfiguredTest {
    checkGTDUOutcome(GotoDeclarationOrUsageHandler2.GTDUOutcome.GTD)
  }

  fun testComponentStandardAttributeSelector() = doConfiguredTest {
    checkGTDUOutcome(GotoDeclarationOrUsageHandler2.GTDUOutcome.GTD)
  }

  fun testEventHandlerOverride() = checkGotoDeclaration(
    284, extension = "html", dir = true, targetFileName = "comp.ts")

  fun testBindingOverride() = checkGotoDeclaration(
    283, extension = "html", dir = true, targetFileName = "comp.ts")

  fun testOneTimeBindingAttribute() = checkGotoDeclaration(
    394, Angular2TestModule.ANGULAR_MATERIAL_7_2_1, extension = "html", targetFileName = "color.d.ts")

}