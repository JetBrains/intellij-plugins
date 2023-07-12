// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.formatting

import com.intellij.application.options.CodeStyle
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.formatter.xml.HtmlCodeStyleSettings
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angularjs.AngularTestUtil

class FormattingTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath(javaClass)
  }

  fun testStyles() {
    doTest("stylesFormatting_after.ts", "stylesFormatting.ts")
  }

  fun testTemplate() {
    doTest("templateFormatting_after.ts", "templateFormatting.ts")
  }

  fun testAttrs() {
    val htmlSettings = settings.getCustomSettings(HtmlCodeStyleSettings::class.java)
    htmlSettings.HTML_ATTRIBUTE_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS
    htmlSettings.HTML_SPACE_AROUND_EQUALITY_IN_ATTRIBUTE = true
    doTest("attrFormatting_after.html", "attrFormatting.html", "attrFormatting.ts")
  }

  fun testInnerAttrs() {
    val htmlSettings = settings.getCustomSettings(HtmlCodeStyleSettings::class.java)
    htmlSettings.HTML_ATTRIBUTE_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS
    htmlSettings.HTML_SPACE_AROUND_EQUALITY_IN_ATTRIBUTE = true
    doTest("innerAttrFormatting_after.ts", "innerAttrFormatting.ts")
  }

  fun testNoKeepLineBreaks() {
    val htmlSettings = settings.getCustomSettings(HtmlCodeStyleSettings::class.java)
    htmlSettings.HTML_ATTRIBUTE_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED
    htmlSettings.HTML_SPACE_AROUND_EQUALITY_IN_ATTRIBUTE = false
    htmlSettings.HTML_KEEP_LINE_BREAKS = false
    htmlSettings.HTML_KEEP_BLANK_LINES = 0
    doTest("noKeepLineBreakFormatting_after.html", "noKeepLineBreakFormatting.html")
  }

  fun testAttributeTyping() {
    myFixture.configureByFiles("attrTyping.html", "package.json")
    myFixture.type("\ntest2\n[test]=\"\"\n[(banana)]=\"\"\nother\n")
    myFixture.checkResultByFile("attrTyping_after.html")
  }

  private fun doTest(expectedFile: String, vararg before: String) {
    myFixture.configureByFile("package.json")
    val psiFile = myFixture.configureByFiles(*before)[0]
    doReformat(psiFile)
    myFixture.checkResultByFile(expectedFile)
  }

  private fun doReformat(file: PsiElement) {
    val codeStyleManager = CodeStyleManager.getInstance(project)
    WriteCommandAction.runWriteCommandAction(project) { codeStyleManager.reformat(file) }
  }

  private val settings: CodeStyleSettings
    get() = CodeStyle.getSettings(project)
}
