// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationOrUsageHandler2
import com.intellij.polySymbols.testFramework.checkGTDUOutcome
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule

class Angular2GotoDeclarationTest : Angular2TestCase("navigation/declaration", false) {

  fun testExportAs() = doGotoDeclarationTest("exportAs: \"<caret>test\"")

  fun testExportAsWithSpaces() = doGotoDeclarationTest("exportAs: \"foo, <caret>test \"")

  fun testExportAsHostDirectives() = doGotoDeclarationTest("exportAs: \"<caret>bold\"")

  fun testComponentStandardElementSelector() = doConfiguredTest {
    checkGTDUOutcome(GotoDeclarationOrUsageHandler2.GTDUOutcome.GTD)
  }

  fun testComponentStandardAttributeSelector() = doConfiguredTest {
    checkGTDUOutcome(GotoDeclarationOrUsageHandler2.GTDUOutcome.GTD)
  }

  fun testEventHandlerOverride() = doGotoDeclarationTest(
    "@Output(\"<caret>complete\")", extension = "html", dir = true, expectedFileName = "comp.ts")

  fun testBindingOverride() = doGotoDeclarationTest(
    "@Input(\"<caret>model\")", extension = "html", dir = true, expectedFileName = "comp.ts")

  fun testOneTimeBindingAttribute() = doGotoDeclarationTest(
    "<caret>color: ThemePalette", Angular2TestModule.ANGULAR_MATERIAL_7_2_1, extension = "html", expectedFileName = "color.d.ts")

  fun testDirectiveInputDecoratedField() = doGotoDeclarationTest("<caret>input: string")

  fun testDirectiveInputFieldDecoratorString() = doGotoDeclarationTest("@Input(\"<caret>aliased\")")

  fun testDirectiveInputFieldDecoratorObject() = doGotoDeclarationTest("alias: \"<caret>aliased\"")

  fun testDirectiveInputForwarded() = doGotoDeclarationTest("<caret>input: string")

  fun testDirectiveInputMappedString() = doGotoDeclarationTest("\"input: <caret>aliased\"")

  fun testDirectiveInputMappedObject() = doGotoDeclarationTest("alias: \"<caret>aliased\"")

  fun testDirectiveInputVirtual() = doGotoDeclarationTest("<caret>virtual")

  fun testDirectiveOutputDecoratedField() = doGotoDeclarationTest("<caret>output: EventEmitter<String>")

  fun testDirectiveOutputFieldDecorator() = doGotoDeclarationTest("@Output(\"<caret>aliased\")")

  fun testDirectiveOutputForwarded() = doGotoDeclarationTest("<caret>output: EventEmitter<String>")

  fun testDirectiveOutputMapped() = doGotoDeclarationTest("outputs: [\"output : <caret>aliased\"]")

  fun testDirectiveOutputVirtual() = doGotoDeclarationTest("outputs: [\"<caret>virtual\"],")

  fun testHostDirectiveInputForwarded() = doGotoDeclarationTest("@Input(\"<caret>aliased\")")

  fun testHostDirectiveInputMapped() = doGotoDeclarationTest("inputs: [\"aliased: <caret>aliasedTwice\"]")

  fun testHostDirectiveOutputForwarded() = doGotoDeclarationTest("@Output(\"<caret>aliased\")")

  fun testHostDirectiveOutputMapped() = doGotoDeclarationTest("outputs: [\"aliased: <caret>aliasedTwice\"]")

  fun testForBlockImplicitVariable() = doGotoDeclarationTest("<!--target--><caret>@for",
                                                             Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  fun testPrivateSetter() = doGotoDeclarationTest("get <caret>bar(): number")

}