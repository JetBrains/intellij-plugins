// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationOrUsageHandler2
import com.intellij.polySymbols.testFramework.checkGTDUOutcome
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.SkipTsGoProxy
import org.angular2.TestNoService
import org.angular2.TestTsGoProxy
import org.junit.Test

@TestNoService
@TestTsGoProxy
class Angular2GotoDeclarationTest : Angular2TestCase("navigation/declaration") {

  @Test
  fun testExportAs() = doGotoDeclarationTest("exportAs: \"<caret>test\"")

  @Test
  fun testExportAsWithSpaces() = doGotoDeclarationTest("exportAs: \"foo, <caret>test \"")

  @Test
  fun testExportAsHostDirectives() = doGotoDeclarationTest("exportAs: \"<caret>bold\"")

  @Test
  fun testComponentStandardElementSelector() = doConfiguredTest {
    checkGTDUOutcome(GotoDeclarationOrUsageHandler2.GTDUOutcome.GTD)
  }

  @Test
  fun testComponentStandardAttributeSelector() = doConfiguredTest {
    checkGTDUOutcome(GotoDeclarationOrUsageHandler2.GTDUOutcome.GTD)
  }

  @Test
  fun testEventHandlerOverride() = doGotoDeclarationTest(
    "@Output(\"<caret>complete\")", extension = "html", dir = true, expectedFileName = "comp.ts")

  @Test
  fun testBindingOverride() = doGotoDeclarationTest(
    "@Input(\"<caret>model\")", extension = "html", dir = true, expectedFileName = "comp.ts")

  @Test
  fun testOneTimeBindingAttribute() = doGotoDeclarationTest(
    "<caret>color: ThemePalette", Angular2TestModule.ANGULAR_MATERIAL_7_2_1, extension = "html", expectedFileName = "color.d.ts")

  @Test
  fun testDirectiveInputDecoratedField() = doGotoDeclarationTest("<caret>input: string")

  @Test
  fun testDirectiveInputFieldDecoratorString() = doGotoDeclarationTest("@Input(\"<caret>aliased\")")

  @Test
  fun testDirectiveInputFieldDecoratorObject() = doGotoDeclarationTest("alias: \"<caret>aliased\"")

  @Test
  fun testDirectiveInputForwarded() = doGotoDeclarationTest("<caret>input: string")

  @Test
  fun testDirectiveInputMappedString() = doGotoDeclarationTest("\"input: <caret>aliased\"")

  @Test
  fun testDirectiveInputMappedObject() = doGotoDeclarationTest("alias: \"<caret>aliased\"")

  @Test
  fun testDirectiveInputVirtual() = doGotoDeclarationTest("<caret>virtual")

  @Test
  fun testDirectiveOutputDecoratedField() = doGotoDeclarationTest("<caret>output: EventEmitter<String>")

  @Test
  fun testDirectiveOutputFieldDecorator() = doGotoDeclarationTest("@Output(\"<caret>aliased\")")

  @Test
  fun testDirectiveOutputForwarded() = doGotoDeclarationTest("<caret>output: EventEmitter<String>")

  @Test
  fun testDirectiveOutputMapped() = doGotoDeclarationTest("outputs: [\"output : <caret>aliased\"]")

  @Test
  fun testDirectiveOutputVirtual() = doGotoDeclarationTest("outputs: [\"<caret>virtual\"],")

  @Test
  fun testHostDirectiveInputForwarded() = doGotoDeclarationTest("@Input(\"<caret>aliased\")")

  @Test
  fun testHostDirectiveInputMapped() = doGotoDeclarationTest("inputs: [\"aliased: <caret>aliasedTwice\"]")

  @Test
  fun testHostDirectiveOutputForwarded() = doGotoDeclarationTest("@Output(\"<caret>aliased\")")

  @Test
  fun testHostDirectiveOutputMapped() = doGotoDeclarationTest("outputs: [\"aliased: <caret>aliasedTwice\"]")

  @Test
  fun testForBlockImplicitVariable() = doGotoDeclarationTest("<!--target--><caret>@for",
                                                             Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")
  @Test
  @SkipTsGoProxy // Flaky
  fun testPrivateSetter() = doGotoDeclarationTest("get <caret>bar(): number")

}