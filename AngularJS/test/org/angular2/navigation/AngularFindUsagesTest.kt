// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationOrUsageHandler2
import com.intellij.javascript.web.usagesAtCaret
import com.intellij.openapi.editor.ex.RangeHighlighterEx
import com.intellij.testFramework.UsefulTestCase
import com.intellij.webSymbols.checkGTDUOutcome
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angularjs.AngularTestUtil
import java.util.*

class AngularFindUsagesTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath(javaClass) + "/findUsages"
  }

  private fun checkUsages(signature: String,
                          vararg usages: String) {
    myFixture.checkGTDUOutcome(GotoDeclarationOrUsageHandler2.GTDUOutcome.SU, signature)
    assertEquals(Arrays.asList(*usages), myFixture.usagesAtCaret())
  }

  fun testPrivateComponentField() {
    myFixture.configureByFiles("private.ts", "private.html", "package.json")
    checkUsages("f<caret>oo",
                "<private.html:(3,6):(0,3)>\tfoo",
                "<private.html:(69,72):(0,3)>\tfoo",
                "<private.ts:(350,358):(5,8)>\tthis.foo")
  }

  fun testPrivateComponentFieldLocalHighlighting() {
    val highlighters = myFixture.testHighlightUsages("private_highlighting.ts", "private.html", "package.json")
    val fileText = myFixture.getFile().getText()
    val actualHighlightWords = highlighters.map {
      val highlighterEx = it as RangeHighlighterEx
      fileText.substring(highlighterEx.getAffectedAreaStartOffset(), highlighterEx.getAffectedAreaEndOffset())
    }
    UsefulTestCase.assertSameElements(actualHighlightWords, "foo")
  }

  fun testPrivateComponentMethod() {
    myFixture.configureByFiles("private.ts", "private.html", "package.json")
    checkUsages("b<caret>ar",
                "<private.html:(13,16):(0,3)>\tbar()",
                "<private.html:(49,52):(0,3)>\tbar()",
                "<private.ts:(369,377):(5,8)>\tthis.bar()")
  }

  fun testPrivateConstructorField() {
    myFixture.configureByFiles("private.ts", "private.html", "package.json")
    checkUsages("fooB<caret>ar",
                "<private.html:(120,126):(0,6)>\tfoo + fooBar",
                "<private.html:(25,31):(0,6)>\tfooBar",
                "<private.ts:(385,396):(5,11)>\tthis.fooBar")
  }

  fun testComponentCustomElementSelector() {
    myFixture.configureByFiles("slots.component.ts", "slots.test.component.html", "slots.test.component.ts", "package.json")
    checkUsages("slots-<caret>component",
                "<slots.test.component.html:(0,187):(1,16)>\tslots-component",
                "<slots.test.component.html:(0,187):(126,141)>\tslots-component")
  }

  fun testComponentStandardElementSelector() {
    myFixture.configureByFiles("standardSelectors.ts", "package.json")
    AngularTestUtil.moveToOffsetBySignature("\"di<caret>v,", myFixture)
    // Cannot find usages of standard tags and attributes, just check outcome
    myFixture.checkGTDUOutcome(GotoDeclarationOrUsageHandler2.GTDUOutcome.GTD)
  }

  fun testComponentStandardAttributeSelector() {
    myFixture.configureByFiles("standardSelectors.ts", "package.json")
    AngularTestUtil.moveToOffsetBySignature(",[cl<caret>ass]", myFixture)
    // Cannot find usages of standard tags and attributes, just check outcome
    myFixture.checkGTDUOutcome(GotoDeclarationOrUsageHandler2.GTDUOutcome.GTD)
  }

  fun testSlotComponentElementSelector() {
    myFixture.configureByFiles("slots.component.ts", "slots.test.component.html", "slots.test.component.ts", "package.json")
    checkUsages("tag<caret>-slot",
                "<slots.test.component.html:(0,187):(21,29)>\ttag-slot",
                "<slots.test.component.html:(0,187):(61,69)>\ttag-slot")
  }

  fun testSlotComponentAttributeSelector() {
    myFixture.configureByFiles("slots.component.ts", "slots.test.component.html", "slots.test.component.ts", "package.json")
    checkUsages("attr<caret>-slot",
                "<slots.test.component.html:(0,187):(78,87)>\tattr-slot")
  }

  fun testAttVariable() {
    myFixture.configureByFiles("attr-variable.html", "attr-variable.ts", "package.json")
    checkUsages("#so<caret>meText", "<attr-variable.ts:(362,372):(1,9)>\t('someText')")
  }
}
