// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.webSymbols.testFramework.moveToOffsetBySignature
import com.jetbrains.plugins.jade.JadeLanguage
import org.jetbrains.plugins.sass.SASSLanguage
import org.jetbrains.plugins.scss.SCSSLanguage
import org.jetbrains.plugins.stylus.StylusLanguage
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.jetbrains.vuejs.lang.html.psi.formatter.VueCodeStyleSettings

class VueTypingTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = getVueTestDataPath() + "/typing"

  fun testStylusEnterEnd() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) { styleSettings ->
      styleSettings.getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = false
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "script"
      }
      styleSettings.getLanguageIndentOptions(StylusLanguage.INSTANCE).INDENT_SIZE = 1
      styleSettings.getLanguageIndentOptions(VueLanguage.INSTANCE).INDENT_SIZE = 5
      styleSettings.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 5
      doTest("\n")
    }
  }

  fun testSassEnterEnd() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) { styleSettings ->
      styleSettings.getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = false
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "script"
      }
      styleSettings.getLanguageIndentOptions(SASSLanguage.INSTANCE).INDENT_SIZE = 4
      styleSettings.getLanguageIndentOptions(VueLanguage.INSTANCE).INDENT_SIZE = 1
      styleSettings.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 1
      doTest("\n")
    }
  }

  fun testPugEnterEnd() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) { styleSettings ->
      styleSettings.getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = false
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "template"
      }
      styleSettings.getLanguageIndentOptions(JadeLanguage.INSTANCE).INDENT_SIZE = 2
      styleSettings.getLanguageIndentOptions(VueLanguage.INSTANCE).INDENT_SIZE = 6
      styleSettings.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 6
      doTest("\n")
    }
  }

  fun testJSEnterStart() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) { styleSettings ->
      styleSettings.getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = false
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "script"
      }
      styleSettings.getLanguageIndentOptions(JavascriptLanguage).INDENT_SIZE = 2
      styleSettings.getLanguageIndentOptions(VueLanguage.INSTANCE).INDENT_SIZE = 6
      styleSettings.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 6
      doTest("\n")
    }
  }

  fun testStylusBackspace() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) { styleSettings ->
      styleSettings.getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = false
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "style"
      }
      styleSettings.getLanguageIndentOptions(StylusLanguage.INSTANCE).INDENT_SIZE = 2
      styleSettings.getLanguageIndentOptions(VueLanguage.INSTANCE).INDENT_SIZE = 5
      styleSettings.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 5
      doTest("\b\b")
    }
  }

  fun testStylusBackspace2() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) { styleSettings ->
      styleSettings.getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = true
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "style"
      }
      styleSettings.getLanguageIndentOptions(StylusLanguage.INSTANCE).INDENT_SIZE = 5
      styleSettings.getLanguageIndentOptions(VueLanguage.INSTANCE).INDENT_SIZE = 2
      styleSettings.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 5
      doTest("StylusBackspace", "\b\b")
    }
  }

  fun testPugBackspace() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) { styleSettings ->
      styleSettings.getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = false
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "template"
      }
      styleSettings.getLanguageIndentOptions(JadeLanguage.INSTANCE).INDENT_SIZE = 2
      styleSettings.getLanguageIndentOptions(VueLanguage.INSTANCE).INDENT_SIZE = 5
      styleSettings.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 5
      doTest("\b\b")
    }
  }

  fun testPugBackspace2() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) { styleSettings ->
      styleSettings.getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = true
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "template"
      }
      styleSettings.getLanguageIndentOptions(JadeLanguage.INSTANCE).INDENT_SIZE = 5
      styleSettings.getLanguageIndentOptions(VueLanguage.INSTANCE).INDENT_SIZE = 2
      styleSettings.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 5
      doTest("PugBackspace", "\b\b")
    }
  }

  fun testSassBackspace() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) { styleSettings ->
      styleSettings.getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = false
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "style"
      }
      styleSettings.getLanguageIndentOptions(SASSLanguage.INSTANCE).INDENT_SIZE = 2
      styleSettings.getLanguageIndentOptions(VueLanguage.INSTANCE).INDENT_SIZE = 5
      styleSettings.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 5
      doTest("\b\b")
    }
  }

  fun testJSBackspace() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) { styleSettings ->
      styleSettings.getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = false
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "script"
      }
      styleSettings.getLanguageIndentOptions(JavascriptLanguage).INDENT_SIZE = 2
      styleSettings.getLanguageIndentOptions(VueLanguage.INSTANCE).INDENT_SIZE = 5
      styleSettings.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 5
      doTest("\b")
    }
  }

  fun testHtmlBackspace() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) { styleSettings ->
      styleSettings.getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = false
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "template"
      }
      styleSettings.getLanguageIndentOptions(VueLanguage.INSTANCE).INDENT_SIZE = 5
      styleSettings.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 2
      doTest("\b")
    }
  }

  fun testScssBackspace() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) { styleSettings ->
      styleSettings.getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = false
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "style"
      }
      styleSettings.getLanguageIndentOptions(SCSSLanguage.INSTANCE).INDENT_SIZE = 2
      styleSettings.getLanguageIndentOptions(VueLanguage.INSTANCE).INDENT_SIZE = 5
      styleSettings.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 5
      doTest("\b")
    }
  }

  fun testInjectedJsonEnterHandler() {
    doTest("{\n\"foo\":12\n")
  }

  // WEB-61505
  fun testAutoStringInterpolation() {
    doTest("{")
  }

  fun testAutoStringInterpolationDirective() {
    doTest("{")
  }

  fun testPasteIntoJsxFreshlyTypedAttr() {
    myFixture.configureByText("test.vue", """
      <script lang="jsx">
      const <selection>drop</selection>down = () => (<div style="foo">a</div>)
      </script>
    """.trimIndent())
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_COPY)
    myFixture.editor.caretModel.primaryCaret.setSelection(0, 0)
    myFixture.moveToOffsetBySignature("\"foo\"<caret>>")
    myFixture.type(" title=\"")
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_PASTE)
    myFixture.checkResult("""
      <script lang="jsx">
      const dropdown = () => (<div style="foo" title="drop">a</div>)
      </script>
    """.trimIndent())
  }

  private fun doTest(toType: String) {
    doTest(getTestName(false), toType)
  }

  private fun doTest(fileName: String, toType: String) {
    myFixture.configureByFile("$fileName.vue")
    myFixture.type(toType)
    myFixture.checkResultByFile("${fileName}_after.vue")
  }
}