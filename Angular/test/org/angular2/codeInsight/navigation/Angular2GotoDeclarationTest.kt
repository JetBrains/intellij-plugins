// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationOrUsageHandler2
import com.intellij.webSymbols.testFramework.checkGTDUOutcome
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule

class Angular2GotoDeclarationTest : Angular2TestCase("navigation/declaration", false) {

  fun testExportAs() = checkGotoDeclaration("exportAs: \"<caret>test\"")

  fun testExportAsWithSpaces() = checkGotoDeclaration("exportAs: \"foo, <caret>test \"")

  fun testExportAsHostDirectives() = checkGotoDeclaration("exportAs: \"<caret>bold\"")

  fun testComponentStandardElementSelector() = doConfiguredTest {
    checkGTDUOutcome(GotoDeclarationOrUsageHandler2.GTDUOutcome.GTD)
  }

  fun testComponentStandardAttributeSelector() = doConfiguredTest {
    checkGTDUOutcome(GotoDeclarationOrUsageHandler2.GTDUOutcome.GTD)
  }

  fun testEventHandlerOverride() = checkGotoDeclaration(
    "@Output(\"<caret>complete\")", extension = "html", dir = true, expectedFileName = "comp.ts")

  fun testBindingOverride() = checkGotoDeclaration(
    "@Input(\"<caret>model\")", extension = "html", dir = true, expectedFileName = "comp.ts")

  fun testOneTimeBindingAttribute() = checkGotoDeclaration(
    "<caret>color: ThemePalette", Angular2TestModule.ANGULAR_MATERIAL_7_2_1, extension = "html", expectedFileName = "color.d.ts")

  fun testDirectiveInputDecoratedField() = checkGotoDeclaration("<caret>input: string")

  fun testDirectiveInputFieldDecoratorString() = checkGotoDeclaration("@Input(\"<caret>aliased\")")

  fun testDirectiveInputFieldDecoratorObject() = checkGotoDeclaration("alias: \"<caret>aliased\"")

  fun testDirectiveInputForwarded() = checkGotoDeclaration("<caret>input: string")

  fun testDirectiveInputMappedString() = checkGotoDeclaration("\"input: <caret>aliased\"")

  fun testDirectiveInputMappedObject() = checkGotoDeclaration("alias: \"<caret>aliased\"")

  fun testDirectiveInputVirtual() = checkGotoDeclaration("<caret>virtual")

  fun testDirectiveOutputDecoratedField() = checkGotoDeclaration("<caret>output: EventEmitter<String>")

  fun testDirectiveOutputFieldDecorator() = checkGotoDeclaration("@Output(\"<caret>aliased\")")

  fun testDirectiveOutputForwarded() = checkGotoDeclaration("<caret>output: EventEmitter<String>")

  fun testDirectiveOutputMapped() = checkGotoDeclaration("outputs: [\"output : <caret>aliased\"]")

  fun testDirectiveOutputVirtual() = checkGotoDeclaration("outputs: [\"<caret>virtual\"],")

  fun testHostDirectiveInputForwarded() = checkGotoDeclaration("@Input(\"<caret>aliased\")")

  fun testHostDirectiveInputMapped() = checkGotoDeclaration("inputs: [\"aliased: <caret>aliasedTwice\"]")

  fun testHostDirectiveOutputForwarded() = checkGotoDeclaration("@Output(\"<caret>aliased\")")

  fun testHostDirectiveOutputMapped() = checkGotoDeclaration("outputs: [\"aliased: <caret>aliasedTwice\"]")

  fun testForBlockImplicitVariable() = checkGotoDeclaration("<!--target--><caret>@for",
                                                            Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

}