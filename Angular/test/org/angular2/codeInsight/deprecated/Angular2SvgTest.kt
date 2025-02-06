// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.deprecated

import com.intellij.util.containers.ContainerUtil
import com.intellij.webSymbols.testFramework.checkListByFile
import com.intellij.webSymbols.testFramework.moveToOffsetBySignature
import com.intellij.webSymbols.testFramework.renderLookupItems
import one.util.streamex.StreamEx
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angular2.Angular2TemplateInspectionsProvider
import org.angular2.Angular2TestModule
import org.angular2.Angular2TestModule.Companion.configureDependencies
import org.angular2.Angular2TestUtil

@Deprecated("Use test appropriate for IDE feature being tested - e.g. completion/resolve/highlighting ")
class Angular2SvgTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return Angular2TestUtil.getBaseTestDataPath() + "deprecated/svg"
  }

  fun testHighlighting() {
    myFixture.configureDependencies(Angular2TestModule.ANGULAR_COMMON_4_0_0, Angular2TestModule.ANGULAR_CORE_4_0_0)
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureByFiles("svg-highlighting.component.svg", "svg-highlighting.component.ts")
    myFixture.checkHighlighting()
  }

  fun testCompletion() {
    myFixture.configureDependencies(Angular2TestModule.ANGULAR_COMMON_4_0_0, Angular2TestModule.ANGULAR_CORE_4_0_0)
    myFixture.configureByFiles("svg-completion.component.svg", "svg-completion.component.ts")
    myFixture.moveToOffsetBySignature("<<caret>paths></paths>")
    myFixture.completeBasic()
    myFixture.checkListByFile(myFixture.renderLookupItems(true, true), "svg-completion.component.txt", false)
  }

  fun testExpressionsCompletion() {
    myFixture.configureDependencies(Angular2TestModule.ANGULAR_COMMON_4_0_0, Angular2TestModule.ANGULAR_CORE_4_0_0)
    myFixture.copyDirectoryToProject(".", ".")
    myFixture.configureByFiles("svg-completion.component.svg", "svg-completion.component.ts")
    myFixture.moveToOffsetBySignature("{{<caret>item.height}}")
    myFixture.completeBasic()
    assertEquals(StreamEx.of(
      "\$any (typeText='any'; priority=4.0; bold)",
      "height (typeText='number'; priority=101.0; bold)",
      "item (typeText=null; priority=101.0; bold)",
      "items (typeText=null; priority=101.0; bold)"
    ).sorted().toList(),
                 ContainerUtil.sorted(Angular2TestUtil.renderLookupItems(myFixture, true, true, true)))
  }

  fun testExpressionsCompletion2() {
    myFixture.configureDependencies(Angular2TestModule.ANGULAR_COMMON_4_0_0, Angular2TestModule.ANGULAR_CORE_4_0_0)
    myFixture.copyDirectoryToProject(".", ".")
    myFixture.configureByFiles("svg-completion.component.svg", "svg-completion.component.ts")
    myFixture.moveToOffsetBySignature("{{item.<caret>height}}")
    myFixture.completeBasic()
    assertEquals(listOf(
      "foo (typeText='string'; priority=101.0; bold)",
      "width (typeText='number'; priority=101.0; bold)",
    ).sorted(), Angular2TestUtil.renderLookupItems(myFixture, true, true, false).sorted())
  }
}
