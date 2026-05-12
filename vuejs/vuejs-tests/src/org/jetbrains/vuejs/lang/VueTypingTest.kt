// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.application.ReadResult
import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.polySymbols.testFramework.moveToOffsetBySignature
import com.jetbrains.plugins.jade.JadeLanguage
import org.jetbrains.plugins.sass.SASSLanguage
import org.jetbrains.plugins.scss.SCSSLanguage
import org.jetbrains.plugins.stylus.StylusLanguage
import org.jetbrains.vuejs.VueTestCase
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.jetbrains.vuejs.lang.html.psi.formatter.VueCodeStyleSettings

class VueTypingTest : VueTestCase("typing") {

  override val testName: String get() = getTestName(false)

  fun testStylusEnterEnd() =
    // For some weird reason, doEditorTypingTest does not work here...
    doConfiguredTest(configureCodeStyleSettings = {
      getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = false
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "script"
      }
      getLanguageIndentOptions(StylusLanguage.INSTANCE).INDENT_SIZE = 1
      getLanguageIndentOptions(VueLanguage).INDENT_SIZE = 5
      getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 5
    }) {
      type("\n")
    }

  fun testSassEnterEnd() =
    // For some weird reason, doEditorTypingTest does not work here...
    doConfiguredTest(configureCodeStyleSettings = {
      getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = false
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "script"
      }
      getLanguageIndentOptions(SASSLanguage.INSTANCE).INDENT_SIZE = 4
      getLanguageIndentOptions(VueLanguage).INDENT_SIZE = 1
      getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 1
    }) {
      type("\n")
    }

  fun testPugEnterEnd() =
    doEditorTypingTest(configureCodeStyleSettings = {
      getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = false
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "template"
      }
      getLanguageIndentOptions(JadeLanguage.INSTANCE).INDENT_SIZE = 2
      getLanguageIndentOptions(VueLanguage).INDENT_SIZE = 6
      getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 6
    }) {
      type("\n")
    }

  fun testJSEnterStart() =
    doEditorTypingTest(configureCodeStyleSettings = {
      getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = false
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "script"
      }
      getLanguageIndentOptions(JavascriptLanguage).INDENT_SIZE = 2
      getLanguageIndentOptions(VueLanguage).INDENT_SIZE = 6
      getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 6
    }) {
      type("\n")
    }

  fun testStylusBackspace() =
    doEditorTypingTest(configureCodeStyleSettings = {
      getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = false
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "style"
      }
      getLanguageIndentOptions(StylusLanguage.INSTANCE).INDENT_SIZE = 2
      getLanguageIndentOptions(VueLanguage).INDENT_SIZE = 5
      getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 5
    }) {
      type("\b\b")
    }

  fun testStylusBackspace2() =
    doEditorTypingTest(configureCodeStyleSettings = {
      getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = true
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "style"
      }
      getLanguageIndentOptions(StylusLanguage.INSTANCE).INDENT_SIZE = 5
      getLanguageIndentOptions(VueLanguage).INDENT_SIZE = 2
      getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 5
    }, configureFileName = "StylusBackspace.vue", goldFileName = "StylusBackspace_after.vue") {
      type("\b\b")
    }

  fun testPugBackspace() =
    doEditorTypingTest(configureCodeStyleSettings = {
      getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = false
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "template"
      }
      getLanguageIndentOptions(JadeLanguage.INSTANCE).INDENT_SIZE = 2
      getLanguageIndentOptions(VueLanguage).INDENT_SIZE = 5
      getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 5
    }) {
      type("\b\b")
    }

  fun testPugBackspace2() =
    doEditorTypingTest(configureCodeStyleSettings = {
      getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = true
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "template"
      }
      getLanguageIndentOptions(JadeLanguage.INSTANCE).INDENT_SIZE = 5
      getLanguageIndentOptions(VueLanguage).INDENT_SIZE = 2
      getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 5
    }, configureFileName = "PugBackspace.vue", goldFileName = "PugBackspace_after.vue") {
      type("\b\b")
    }

  fun testSassBackspace() =
    doEditorTypingTest(configureCodeStyleSettings = {
      getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = false
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "style"
      }
      getLanguageIndentOptions(SASSLanguage.INSTANCE).INDENT_SIZE = 2
      getLanguageIndentOptions(VueLanguage).INDENT_SIZE = 5
      getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 5
    }) {
      type("\b\b")
    }

  fun testJSBackspace() =
    doEditorTypingTest(configureCodeStyleSettings = {
      getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = false
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "script"
      }
      getLanguageIndentOptions(JavascriptLanguage).INDENT_SIZE = 2
      getLanguageIndentOptions(VueLanguage).INDENT_SIZE = 5
      getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 5
    }) {
      type("\b")
    }

  fun testHtmlBackspace() =
    doEditorTypingTest(configureCodeStyleSettings = {
      getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = false
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "template"
      }
      getLanguageIndentOptions(VueLanguage).INDENT_SIZE = 5
      getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 2
    }) {
      type("\b")
    }

  fun testScssBackspace() =
    doEditorTypingTest(configureCodeStyleSettings = {
      getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = false
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "style"
      }
      getLanguageIndentOptions(SCSSLanguage.INSTANCE).INDENT_SIZE = 2
      getLanguageIndentOptions(VueLanguage).INDENT_SIZE = 5
      getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 5
    }) {
      type("\b")
    }

  fun testInjectedJsonEnterHandler() =
    doEditorTypingTest {
      type("{\n\"foo\":12\n")
    }

  // WEB-61505
  fun testAutoStringInterpolation() =
    doEditorTypingTest {
      type("{")

    }

  fun testAutoStringInterpolationDirective() =
    doEditorTypingTest {
      type("{")
    }

  fun testPasteIntoJsxFreshlyTypedAttr() =
    doEditorTypingTest {
      performEditorAction(IdeActions.ACTION_EDITOR_COPY)
      invokeAndWaitIfNeeded {
        editor.caretModel.primaryCaret.setSelection(0, 0)
      }
      moveToOffsetBySignature("\"foo\"<caret>>")
      type(" title=\"")
      performEditorAction(IdeActions.ACTION_EDITOR_PASTE)
    }

}