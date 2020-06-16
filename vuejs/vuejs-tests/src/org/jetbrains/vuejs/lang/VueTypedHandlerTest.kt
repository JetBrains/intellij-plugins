// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.vuejs.lang

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.util.Pair
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.NullableFunction
import org.jetbrains.vuejs.editor.VueInterpolationBracesCompleter
import org.jetbrains.vuejs.lang.html.VueFileType

class VueTypedHandlerTest : BasePlatformTestCase() {
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
    myFixture.configureByText(HtmlFileType.INSTANCE, "#1<caret>$")
    doInterpolationBracesCompleterTest("#", "$", '$', false)
    myFixture.checkResult("#1$<caret>")
  }

  @Throws(Exception::class)
  fun testDoNotCompleteEndWithoutStart() {
    myFixture.configureByText(HtmlFileType.INSTANCE, "abc {<caret>")
    doInterpolationBracesCompleterTest("{{", "}}", '}', false)
    myFixture.checkResult("abc {}<caret>")
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
<script>{{<caret>}</script>
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

  fun testBracesCompletionInsideVueScriptContents() {
    myFixture.configureByText(VueFileType.INSTANCE, """
<template></template>
<script>
export default <caret>
</script>
""")
    doInterpolationBracesCompleterTest("{{", "}}", '{', false)
    myFixture.checkResult("""
<template></template>
<script>
export default {<caret>}
</script>
""")
  }

  fun testTypeClosingCol() {
    myFixture.configureByText("TypeClosingCol.vue", """
<template>
  <div>
    <Col><<caret>
  </div>
</template>
<script lang="es6">
  export default {
    components: {
      Col: {}
    }
  }
</script>
""")
    myFixture.type("/")
    myFixture.checkResult("""
<template>
  <div>
    <Col></Col>
  </div>
</template>
<script lang="es6">
  export default {
    components: {
      Col: {}
    }
  }
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
