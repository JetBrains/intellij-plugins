// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.javascript.web.WebFrameworkTestModule
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.formatter.xml.HtmlCodeStyleSettings
import org.angular2.Angular2TestCase

class Angular2FormattingTest : Angular2TestCase("formatting") {

  fun testStyles() = testFormatting()

  fun testTemplate() = testFormatting()

  fun testAttrs() = testFormatting(extension = "html") {
    val htmlSettings = getCustomSettings(HtmlCodeStyleSettings::class.java)
    htmlSettings.HTML_ATTRIBUTE_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS
    htmlSettings.HTML_SPACE_AROUND_EQUALITY_IN_ATTRIBUTE = true
  }

  fun testInnerAttrs() = testFormatting {
    val htmlSettings = getCustomSettings(HtmlCodeStyleSettings::class.java)
    htmlSettings.HTML_ATTRIBUTE_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS
    htmlSettings.HTML_SPACE_AROUND_EQUALITY_IN_ATTRIBUTE = true
  }

  fun testNoKeepLineBreaks() = testFormatting(extension = "html") {
    val htmlSettings = getCustomSettings(HtmlCodeStyleSettings::class.java)
    htmlSettings.HTML_ATTRIBUTE_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED
    htmlSettings.HTML_SPACE_AROUND_EQUALITY_IN_ATTRIBUTE = false
    htmlSettings.HTML_KEEP_LINE_BREAKS = false
    htmlSettings.HTML_KEEP_BLANK_LINES = 0
  }

  fun testAttributeTyping() = doConfiguredTest(extension = "html", checkResult = true) {
    myFixture.type("\ntest2\n[test]=\"\"\n[(banana)]=\"\"\nother\n")
  }

  private fun testFormatting(vararg modules: WebFrameworkTestModule,
                             dir: Boolean = false,
                             extension: String = "ts",
                             configureCodeStyleSettings: CodeStyleSettings.() -> Unit = {}) =
    doConfiguredTest(*modules, dir = dir, extension = extension, checkResult = true, configureCodeStyleSettings = configureCodeStyleSettings) {
      val codeStyleManager = CodeStyleManager.getInstance(project)
      WriteCommandAction.runWriteCommandAction(project) { codeStyleManager.reformat(file) }
    }

}
