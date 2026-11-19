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
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.util.Pair
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.util.NullableFunction
import kotlinx.coroutines.Dispatchers
import org.jetbrains.vuejs.editor.VueInterpolationBracesCompleter
import org.jetbrains.vuejs.lang.html.VueFileType
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

@TestApplication
class VueTypedHandlerTest {
  private val hybrid by hybridFixture("")
  private val myFixture get() = hybrid.fixture

  @Test
  fun testBracketsClosing(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByText(VueFileType, "{<caret>")
    myFixture.type("{")
    myFixture.checkResult("{{<caret>}}")
  }

  @Test
  fun testBracketsNotClosingTwice(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByText(VueFileType, "{<caret>}}")
    myFixture.type("{")
    myFixture.checkResult("{{<caret>}}")
  }

  @Test
  fun testBracketsNotBreakingAtEnd(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByText(VueFileType, "{{<caret>")
    myFixture.type("}")
    myFixture.checkResult("{{}}<caret>")
  }

  @Test
  fun testClosingBracketsSkipped(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByText(VueFileType, "{{<caret>}}")
    myFixture.type("}")
    myFixture.checkResult("{{}<caret>}")
  }

  @Test
  fun testSecondClosingBracket(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByText(VueFileType, "{{}<caret>")
    myFixture.type("}")
    myFixture.checkResult("{{}}<caret>")
  }

  @Test
  fun testInsertWhitespace(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByText(VueFileType, "{<caret>")
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
  @Test
  fun testOneSymbolDelimiterStartCompletes(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByText(VueFileType, "<caret>")
    doInterpolationBracesCompleterTest("$", "#", '$', true)
    myFixture.checkResult("$ <caret> #")
  }

  @Throws(Exception::class)
  @Test
  fun testMixedDelimitersAlreadyHasEnding(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByText(HtmlFileType.INSTANCE, "$<caret>#")
    doInterpolationBracesCompleterTest("$$", "#", '$', false)
    myFixture.checkResult("$$<caret>#")
  }

  @Throws(Exception::class)
  @Test
  fun testMixedDelimitersCompletionNoStartTypeOver(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByText(HtmlFileType.INSTANCE, "$<caret>$")
    doInterpolationBracesCompleterTest("$$", "#", '$', false)
    myFixture.checkResult("$$<caret>#$")
  }

  @Throws(Exception::class)
  @Test
  fun testOneSymbolDelimiterEndAdded(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByText(HtmlFileType.INSTANCE, "$<caret>")
    doInterpolationBracesCompleterTest("$", "$", '$', false)
    myFixture.checkResult("$$<caret>")
  }

  @Throws(Exception::class)
  @Test
  fun testOneSymbolDelimiterTypeOver(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByText(HtmlFileType.INSTANCE, "$<caret>$")
    doInterpolationBracesCompleterTest("$", "$", '$', false)
    myFixture.checkResult("$$<caret>")
  }

  @Throws(Exception::class)
  @Test
  fun testOneSymbolDelimiterTypeOverOneSymbol(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByText(HtmlFileType.INSTANCE, "#1<caret>$")
    doInterpolationBracesCompleterTest("#", "$", '$', false)
    myFixture.checkResult("#1$<caret>")
  }

  @Throws(Exception::class)
  @Test
  fun testDoNotCompleteEndWithoutStart(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByText(HtmlFileType.INSTANCE, "abc {<caret>")
    doInterpolationBracesCompleterTest("{{", "}}", '}', false)
    myFixture.checkResult("abc {}<caret>")
  }

  @Throws(Exception::class)
  @Test
  fun testThreeSymbolDelimiters(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByText(HtmlFileType.INSTANCE, "{{<caret>")
    doInterpolationBracesCompleterTest("{{{", "}}}", '{', false)
    myFixture.checkResult("{{{<caret>}}}")
  }

  @Throws(Exception::class)
  @Test
  fun testThreeSymbolDelimitersEndTypeOver(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByText(HtmlFileType.INSTANCE, "{{{text}}<caret>}")
    doInterpolationBracesCompleterTest("{{{", "}}}", '}', false)
    myFixture.checkResult("{{{text}}}<caret>")
  }

  @Test
  fun testNoInterpolationInsideScript(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByText(VueFileType, """
<template></template>
<script>{<caret></script>
""")
    doInterpolationBracesCompleterTest("{{", "}}", '{', false)
    myFixture.checkResult("""
<template></template>
<script>{{<caret>}</script>
""")
  }

  @Test
  fun testNoInterpolationInsideScriptContents(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
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

  @Test
  fun testBracesCompletionInsideVueScriptContents(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByText(VueFileType, """
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

  @Test
  fun testTypeClosingCol(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
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
