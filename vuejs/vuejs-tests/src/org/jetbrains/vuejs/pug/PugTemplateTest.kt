// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.pug

import com.intellij.lang.javascript.inspections.JSIncompatibleTypesComparisonInspection
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.getVueTestDataPath
import org.junit.jupiter.api.Test

@TestApplication
class PugTemplateTest {
  private val hybrid by hybridFixture(getVueTestDataPath() + "/pug/")
  private val myFixture get() = hybrid.fixture

  @Test
  fun testJadeExtendsResolve() {
    myFixture.configureVueDependencies()
    myFixture.copyDirectoryToProject("jadeExtends", ".")
    myFixture.configureFromTempProjectFile("test.vue")
    myFixture.checkHighlighting()
  }

  @Test
  fun testPugExtendsResolve() {
    myFixture.configureVueDependencies()
    myFixture.copyDirectoryToProject("pugExtends", ".")
    myFixture.configureFromTempProjectFile("test.vue")
    myFixture.checkHighlighting()
  }

  @Test
  fun testInjectedExpressions() {
    myFixture.enableInspections(JSIncompatibleTypesComparisonInspection())
    myFixture.configureByFile("injectedExpressions.vue")
    myFixture.checkHighlighting()
  }

  @Test
  fun testLineCommenterWithinTemplate() {
    doCommenterTest(true)
  }

  @Test
  fun testLineCommenterCaret() {
    doCommenterTest(true)
  }

  @Test
  fun testLineCommenterAcrossTemplate() {
    doCommenterTest(true)
  }

  @Test
  fun testLineCommenterBinding() {
    doCommenterTest(true)
  }

  @Test
  fun testBlockCommenterBinding() {
    doCommenterTest(false)
  }

  @Test
  fun testHtmlTagTyping() {
    myFixture.configureByFile("htmlTagTyping.vue")
    myFixture.type("te")
    myFixture.completeBasic()
    myFixture.type("\n")
    myFixture.checkResultByFile("htmlTagTyping_after.vue")
  }

  @Test
  fun testComponentCompletion() {
    myFixture.configureByFiles("componentCompletion.vue", "htmlTagTyping.vue")
    myFixture.completeBasic()
    assertThat(myFixture.lookupElementStrings!!).contains("HtmlTagTyping", "html-tag-typing",
                           "template", "component", "suspense", "teleport")
  }

  private fun doCommenterTest(lineCommenter: Boolean) {
    val name = hybrid.getTestName(true)
    myFixture.configureByFile("$name.vue")
    myFixture.performEditorAction(if (lineCommenter) "CommentByLineComment" else "CommentByBlockComment")
    myFixture.checkResultByFile("${name}_after.vue")
  }

}
