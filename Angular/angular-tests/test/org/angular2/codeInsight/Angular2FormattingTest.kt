// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.codeInsight.actions.OptimizeImportsProcessor
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.polySymbols.testFramework.findOffsetBySignature
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.formatter.xml.HtmlCodeStyleSettings
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.TestNoService
import org.angular2.TestTsGoFork
import org.angular2.lang.html.psi.formatter.Angular2HtmlCodeStyleSettings
import org.junit.Test

@TestNoService
@TestTsGoFork
class Angular2FormattingTest : Angular2TestCase("formatting") {

  @Test
  fun testStyles() = doFormattingTest()

  @Test
  fun testTemplate() = doFormattingTest()

  @Test
  fun testAttrs() = doFormattingTest(extension = "html") {
    val htmlSettings = getCustomSettings(HtmlCodeStyleSettings::class.java)
    htmlSettings.HTML_ATTRIBUTE_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS
    htmlSettings.HTML_SPACE_AROUND_EQUALITY_IN_ATTRIBUTE = true
  }

  @Test
  fun testInnerAttrs() = doFormattingTest {
    val htmlSettings = getCustomSettings(HtmlCodeStyleSettings::class.java)
    htmlSettings.HTML_ATTRIBUTE_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS
    htmlSettings.HTML_SPACE_AROUND_EQUALITY_IN_ATTRIBUTE = true
  }

  @Test
  fun testNoKeepLineBreaks() = doFormattingTest(extension = "html") {
    val htmlSettings = getCustomSettings(HtmlCodeStyleSettings::class.java)
    htmlSettings.HTML_ATTRIBUTE_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED
    htmlSettings.HTML_SPACE_AROUND_EQUALITY_IN_ATTRIBUTE = false
    htmlSettings.HTML_KEEP_LINE_BREAKS = false
    htmlSettings.HTML_KEEP_BLANK_LINES = 0
  }

  @Test
  fun testAttributeTyping() = doConfiguredTest(extension = "html", checkResult = true) {
    myFixture.type("\ntest2\n[test]=\"\"\n[(banana)]=\"\"\nother\n")
  }

  @Test
  fun testInterpolationNoNewLineDoNotWrap() = testInterpolation(
    newLineAfterStart = false,
    newLineBeforeEnd = false,
    wrap = CommonCodeStyleSettings.DO_NOT_WRAP
  )

  @Test
  fun testInterpolationNoNewLineWrapAsNeeded() = testInterpolation(
    newLineAfterStart = false,
    newLineBeforeEnd = false,
    wrap = CommonCodeStyleSettings.WRAP_AS_NEEDED
  )

  @Test
  fun testInterpolationNoNewLineWrapAlways() = testInterpolation(
    newLineAfterStart = false,
    newLineBeforeEnd = false,
    wrap = CommonCodeStyleSettings.WRAP_ALWAYS
  )

  @Test
  fun testInterpolationNewLineDoNotWrap() = testInterpolation(
    newLineAfterStart = true,
    newLineBeforeEnd = true,
    wrap = CommonCodeStyleSettings.DO_NOT_WRAP
  )

  @Test
  fun testInterpolationNewLineWrapAsNeeded() = testInterpolation(
    newLineAfterStart = true,
    newLineBeforeEnd = true,
    wrap = CommonCodeStyleSettings.WRAP_AS_NEEDED
  )

  @Test
  fun testInterpolationNewLineWrapAlways() = testInterpolation(
    newLineAfterStart = true,
    newLineBeforeEnd = true,
    wrap = CommonCodeStyleSettings.WRAP_ALWAYS
  )

  @Test
  fun testInterpolationNewLineBeforeEnd() = testInterpolation(
    newLineAfterStart = false,
    newLineBeforeEnd = true,
    wrap = CommonCodeStyleSettings.WRAP_AS_NEEDED
  )

  @Test
  fun testInterpolationNewLineAfterStart() = testInterpolation(
    newLineAfterStart = true,
    newLineBeforeEnd = false,
    wrap = CommonCodeStyleSettings.WRAP_AS_NEEDED
  )

  @Test
  fun testBasicBlocks() = doFormattingTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  @Test
  fun testDeferBlock() = doFormattingTest(Angular2TestModule.ANGULAR_CORE_17_3_0, extension = "html")

  @Test
  fun testLetBlock() = doFormattingTest(Angular2TestModule.ANGULAR_CORE_18_2_1, extension = "html")

  @Test
  fun testEditorConfigWithInjection() = doFormattingTest(dir = true, editorConfigEnabled = true)

  @Test
  fun testEditorConfigWithinInjection() = doConfiguredTest(dir = true, checkResult = true, editorConfigEnabled = true) {
    WriteCommandAction.runWriteCommandAction(project) {
      PsiDocumentManager.getInstance(project).commitAllDocuments()
      val injectedFile = InjectedLanguageUtil.findElementAtNoCommit(file, file.findOffsetBySignature("*<caret>cdkVirtualFor"))
        .containingFile
      val codeStyleManager = CodeStyleManager.getInstance(project)
      codeStyleManager.reformat(injectedFile)
    }
  }

  @Test
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

  @Test
  fun testReferenceDeclaration() = doFormattingTest(extension = "html") {
    val tsSettings = getCustomSettings(TypeScriptCodeStyleSettings::class.java)
    tsSettings.VAR_DECLARATION_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS
    val jsSettings = getCustomSettings(JSCodeStyleSettings::class.java)
    jsSettings.VAR_DECLARATION_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS
  }

  @Test
  fun testForBlockVariableDeclaration() = doFormattingTest(extension = "html") {
    val tsSettings = getCustomSettings(TypeScriptCodeStyleSettings::class.java)
    tsSettings.VAR_DECLARATION_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS
    val jsSettings = getCustomSettings(JSCodeStyleSettings::class.java)
    jsSettings.VAR_DECLARATION_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS
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
