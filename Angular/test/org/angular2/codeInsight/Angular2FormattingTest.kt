// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.codeInsight.actions.OptimizeImportsProcessor
import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.formatter.xml.HtmlCodeStyleSettings
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil
import com.intellij.webSymbols.testFramework.findOffsetBySignature
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.lang.html.psi.formatter.Angular2HtmlCodeStyleSettings

class Angular2FormattingTest : Angular2TestCase("formatting", false) {

  fun testStyles() = doFormattingTest()

  fun testTemplate() = doFormattingTest()

  fun testAttrs() = doFormattingTest(extension = "html") {
    val htmlSettings = getCustomSettings(HtmlCodeStyleSettings::class.java)
    htmlSettings.HTML_ATTRIBUTE_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS
    htmlSettings.HTML_SPACE_AROUND_EQUALITY_IN_ATTRIBUTE = true
  }

  fun testInnerAttrs() = doFormattingTest {
    val htmlSettings = getCustomSettings(HtmlCodeStyleSettings::class.java)
    htmlSettings.HTML_ATTRIBUTE_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS
    htmlSettings.HTML_SPACE_AROUND_EQUALITY_IN_ATTRIBUTE = true
  }

  fun testNoKeepLineBreaks() = doFormattingTest(extension = "html") {
    val htmlSettings = getCustomSettings(HtmlCodeStyleSettings::class.java)
    htmlSettings.HTML_ATTRIBUTE_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED
    htmlSettings.HTML_SPACE_AROUND_EQUALITY_IN_ATTRIBUTE = false
    htmlSettings.HTML_KEEP_LINE_BREAKS = false
    htmlSettings.HTML_KEEP_BLANK_LINES = 0
  }

  fun testAttributeTyping() = doConfiguredTest(extension = "html", checkResult = true) {
    myFixture.type("\ntest2\n[test]=\"\"\n[(banana)]=\"\"\nother\n")
  }

  fun testInterpolationNoNewLineDoNotWrap() = testInterpolation(
    newLineAfterStart = false,
    newLineBeforeEnd = false,
    wrap = CommonCodeStyleSettings.DO_NOT_WRAP
  )

  fun testInterpolationNoNewLineWrapAsNeeded() = testInterpolation(
    newLineAfterStart = false,
    newLineBeforeEnd = false,
    wrap = CommonCodeStyleSettings.WRAP_AS_NEEDED
  )

  fun testInterpolationNoNewLineWrapAlways() = testInterpolation(
    newLineAfterStart = false,
    newLineBeforeEnd = false,
    wrap = CommonCodeStyleSettings.WRAP_ALWAYS
  )

  fun testInterpolationNewLineDoNotWrap() = testInterpolation(
    newLineAfterStart = true,
    newLineBeforeEnd = true,
    wrap = CommonCodeStyleSettings.DO_NOT_WRAP
  )

  fun testInterpolationNewLineWrapAsNeeded() = testInterpolation(
    newLineAfterStart = true,
    newLineBeforeEnd = true,
    wrap = CommonCodeStyleSettings.WRAP_AS_NEEDED
  )

  fun testInterpolationNewLineWrapAlways() = testInterpolation(
    newLineAfterStart = true,
    newLineBeforeEnd = true,
    wrap = CommonCodeStyleSettings.WRAP_ALWAYS
  )

  fun testInterpolationNewLineBeforeEnd() = testInterpolation(
    newLineAfterStart = false,
    newLineBeforeEnd = true,
    wrap = CommonCodeStyleSettings.WRAP_AS_NEEDED
  )

  fun testInterpolationNewLineAfterStart() = testInterpolation(
    newLineAfterStart = true,
    newLineBeforeEnd = false,
    wrap = CommonCodeStyleSettings.WRAP_AS_NEEDED
  )

  fun testBasicBlocks() = doFormattingTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  fun testDeferBlock() = doFormattingTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  fun testLetBlock() = doFormattingTest(Angular2TestModule.ANGULAR_CORE_18_2_1, extension = "html")

  fun testEditorConfigWithInjection() = doFormattingTest(dir = true, editorConfigEnabled = true)

  fun testEditorConfigWithinInjection() = doConfiguredTest(dir = true, checkResult = true, editorConfigEnabled = true) {
    WriteCommandAction.runWriteCommandAction(project) {
      PsiDocumentManager.getInstance(project).commitAllDocuments()
      val injectedFile = InjectedLanguageUtil.findElementAtNoCommit(file, file.findOffsetBySignature("*<caret>cdkVirtualFor"))
        .containingFile
      val codeStyleManager = CodeStyleManager.getInstance(project)
      codeStyleManager.reformat(injectedFile)
    }
  }

  fun testUnusedComponentImports() =
    doConfiguredTest(Angular2TestModule.ANGULAR_CORE_17_3_0, Angular2TestModule.ANGULAR_COMMON_17_3_0,
                     checkResult = true,
                     additionalFiles = listOf("unusedComponentImports.html")) {
      val codeStyleManager = CodeStyleManager.getInstance(project)
      WriteCommandAction.runWriteCommandAction(project) {
        codeStyleManager.reformat(file)
        OptimizeImportsProcessor(project, file).runWithoutProgress()
      }
    }

  private fun testInterpolation(newLineAfterStart: Boolean, newLineBeforeEnd: Boolean, wrap: Int) =
    doFormattingTest(configureFileName = "interpolation.html") {
      val vueSettings = getCustomSettings(Angular2HtmlCodeStyleSettings::class.java)
      vueSettings.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS = false
      getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 4
      vueSettings.INTERPOLATION_NEW_LINE_BEFORE_END_DELIMITER = newLineBeforeEnd
      vueSettings.INTERPOLATION_NEW_LINE_AFTER_START_DELIMITER = newLineAfterStart
      vueSettings.INTERPOLATION_WRAP = wrap
    }

}
