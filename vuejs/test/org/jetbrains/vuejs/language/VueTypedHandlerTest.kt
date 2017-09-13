package org.jetbrains.vuejs.language

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.util.Pair
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import com.intellij.util.NullableFunction
import org.jetbrains.vuejs.VueFileType

/**
 * @author Irina.Chernushina on 7/25/2017.
 */
class VueTypedHandlerTest : LightPlatformCodeInsightFixtureTestCase() {
  fun testBracketsClosing() {
    myFixture.configureByText(VueFileType.INSTANCE, "{<caret>")
    myFixture.type("{")
    myFixture.checkResult("{{<caret>}}")
  }

  fun testBracketsNotClosingTwice() {
    myFixture.configureByText(VueFileType.INSTANCE, "{<caret>}}")
    myFixture.type("{")
    myFixture.checkResult("{{<caret>}}")
  }

  fun testBracketsNotBreakingAtEnd() {
    myFixture.configureByText(VueFileType.INSTANCE, "{{<caret>")
    myFixture.type("}")
    myFixture.checkResult("{{}}<caret>")
  }

  fun testClosingBracketsSkipped() {
    myFixture.configureByText(VueFileType.INSTANCE, "{{<caret>}}")
    myFixture.type("}")
    myFixture.checkResult("{{}<caret>}")
  }

  fun testSecondClosingBracket() {
    myFixture.configureByText(VueFileType.INSTANCE, "{{}<caret>")
    myFixture.type("}")
    myFixture.checkResult("{{}}<caret>")
  }

  fun testInsertWhitespace() {
    myFixture.configureByText(VueFileType.INSTANCE, "{<caret>")
    val settings = JSCodeStyleSettings.getSettings(myFixture.file)
    val oldWhitespace = settings.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS
    try {
      settings.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS = true
      myFixture.type("{")
      myFixture.checkResult("{{ <caret> }}")
    }
    finally {
      settings.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS = oldWhitespace
    }
  }

  @Throws(Exception::class)
  fun testOneSymbolDelimiterStartCompletes() {
    myFixture.configureByText(VueFileType.INSTANCE, "<caret>")
    doInterpolationBracesCompleterTest("$", "#", '$', true)
    myFixture.checkResult("$ <caret> #")
  }

  @Throws(Exception::class)
  fun testMixedDelimitersAlreadyHasEnding() {
    myFixture.configureByText(HtmlFileType.INSTANCE, "$<caret>#")
    doInterpolationBracesCompleterTest("$$", "#", '$', false)
    myFixture.checkResult("$$<caret>#")
  }

  @Throws(Exception::class)
  fun testMixedDelimitersCompletionNoStartTypeOver() {
    myFixture.configureByText(HtmlFileType.INSTANCE, "$<caret>$")
    doInterpolationBracesCompleterTest("$$", "#", '$', false)
    myFixture.checkResult("$$<caret>#$")
  }

  @Throws(Exception::class)
  fun testOneSymbolDelimiterEndAdded() {
    myFixture.configureByText(HtmlFileType.INSTANCE, "$<caret>")
    doInterpolationBracesCompleterTest("$", "$", '$', false)
    myFixture.checkResult("$$<caret>")
  }

  @Throws(Exception::class)
  fun testOneSymbolDelimiterTypeOver() {
    myFixture.configureByText(HtmlFileType.INSTANCE, "$<caret>$")
    doInterpolationBracesCompleterTest("$", "$", '$', false)
    myFixture.checkResult("$$<caret>")
  }

  @Throws(Exception::class)
  fun testOneSymbolDelimiterTypeOverOneSymbol() {
    myFixture.configureByText(HtmlFileType.INSTANCE, "<caret>$")
    doInterpolationBracesCompleterTest("#", "$", '$', false)
    myFixture.checkResult("$<caret>")
  }

  @Throws(Exception::class)
  fun testThreeSymbolDelimiters() {
    myFixture.configureByText(HtmlFileType.INSTANCE, "{{<caret>")
    doInterpolationBracesCompleterTest("{{{", "}}}", '{', false)
    myFixture.checkResult("{{{<caret>}}}")
  }

  @Throws(Exception::class)
  fun testThreeSymbolDelimitersEndTypeOver() {
    myFixture.configureByText(HtmlFileType.INSTANCE, "{{{text}}<caret>}")
    doInterpolationBracesCompleterTest("{{{", "}}}", '}', false)
    myFixture.checkResult("{{{text}}}<caret>")
  }

  fun testNoInterpolationInsideScript() {
    myFixture.configureByText(VueFileType.INSTANCE, """
<template></template>
<script>{<caret></script>
""")
    doInterpolationBracesCompleterTest("{{", "}}", '{', false)
    myFixture.checkResult("""
<template></template>
<script>{{<caret></script>
""")
  }

  fun testNoInterpolationInsideScriptContents() {
    myFixture.configureByText(HtmlFileType.INSTANCE, """
<template></template>
<script>{<caret>
export default
</script>
""")
    doInterpolationBracesCompleterTest("{{", "}}", '{', false)
    myFixture.checkResult("""
<template></template>
<script>{{<caret>}
export default
</script>
""")
  }

  private fun doInterpolationBracesCompleterTest(start: String,
                                                 end: String,
                                                 typed: Char, addSpace: Boolean) {
    CommandProcessor.getInstance().executeCommand(
      myFixture.project,
      {
        WriteAction.run<RuntimeException> {
          val settings = JSCodeStyleSettings.getSettings(myFixture.file)
          val oldWhitespace = settings.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS
          try {
            settings.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS = addSpace
            val result = VueInterpolationBracesCompleter(NullableFunction { Pair.create(start, end) })
              .beforeCharTyped(typed, myFixture.project, myFixture.editor, myFixture.file)
            if (TypedHandlerDelegate.Result.CONTINUE == result) {
              myFixture.type(typed)
            }
          }
          finally {
            settings.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS = oldWhitespace
          }
        }
      }, null, null)
  }
}