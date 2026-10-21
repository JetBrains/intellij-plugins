// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.polySymbols.testFramework.HybridFixtureContext
import com.intellij.testFramework.junit5.TestApplication
import com.jetbrains.plugins.jade.JadeLanguage
import org.jetbrains.plugins.sass.SASSLanguage
import org.jetbrains.plugins.scss.SCSSLanguage
import org.jetbrains.plugins.stylus.StylusLanguage
import org.jetbrains.vuejs.VueFixture
import org.jetbrains.vuejs.VueTestMode
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.jetbrains.vuejs.lang.html.psi.formatter.VueCodeStyleSettings
import org.jetbrains.vuejs.vueFixture
import org.junit.jupiter.api.Test

@TestApplication
class VueTypingTest {

  private val vue: VueTypingFixture by vueFixture(VueTestMode.DEFAULT) { VueTypingFixture(it) }

  @Test
  fun testStylusEnterEnd() =
    // For some weird reason, doEditorTypingTest does not work here...
    vue.doConfiguredTest(configureCodeStyleSettings = {
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

  @Test
  fun testSassEnterEnd() =
    // For some weird reason, doEditorTypingTest does not work here...
    vue.doConfiguredTest(configureCodeStyleSettings = {
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

  @Test
  fun testPugEnterEnd() =
    vue.doEditorTypingTest(configureCodeStyleSettings = {
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

  @Test
  fun testJSEnterStart() =
    vue.doEditorTypingTest(configureCodeStyleSettings = {
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

  @Test
  fun testStylusBackspace() =
    vue.doEditorTypingTest(configureCodeStyleSettings = {
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

  @Test
  fun testStylusBackspace2() =
    vue.doEditorTypingTest(configureCodeStyleSettings = {
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

  @Test
  fun testPugBackspace() =
    vue.doEditorTypingTest(configureCodeStyleSettings = {
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

  @Test
  fun testPugBackspace2() =
    vue.doEditorTypingTest(configureCodeStyleSettings = {
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

  @Test
  fun testSassBackspace() =
    vue.doEditorTypingTest(configureCodeStyleSettings = {
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

  @Test
  fun testJSBackspace() =
    vue.doEditorTypingTest(configureCodeStyleSettings = {
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

  @Test
  fun testHtmlBackspace() =
    vue.doEditorTypingTest(configureCodeStyleSettings = {
      getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = false
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "template"
      }
      getLanguageIndentOptions(VueLanguage).INDENT_SIZE = 5
      getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 2
    }) {
      type("\b")
    }

  @Test
  fun testScssBackspace() =
    vue.doEditorTypingTest(configureCodeStyleSettings = {
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

  @Test
  fun testInjectedJsonEnterHandler() =
    vue.doEditorTypingTest {
      type("{\n\"foo\":12\n")
    }

  // WEB-61505
  @Test
  fun testAutoStringInterpolation() =
    vue.doEditorTypingTest {
      type("{")

    }

  @Test
  fun testAutoStringInterpolationDirective() =
    vue.doEditorTypingTest {
      type("{")
    }

  @Test
  fun testPasteIntoJsxFreshlyTypedAttr() =
    vue.doEditorTypingTest {
      performEditorAction(IdeActions.ACTION_EDITOR_COPY)
      invokeAndWaitIfNeeded {
        editor.caretModel.primaryCaret.setSelection(0, 0)
      }
      moveToOffsetBySignature("\"foo\"<caret>>")
      type(" title=\"")
      performEditorAction(IdeActions.ACTION_EDITOR_PASTE)
    }

  @Test
  fun testInvalidElementOnTyping() =
    vue.doEditorTypingTest(checkResult = false) {
      // It is possible to get PsiInvalidElement exception here,
      // if caching of source component info is incorrect.
      performHighlighting()
      type("}")
      performHighlighting()
    }

}

/**
 * The state of [VueTypingTest]: a [VueFixture] whose test name keeps the first letter of the test method name.
 */
private class VueTypingFixture(context: HybridFixtureContext) : VueFixture(context, "typing", VueTestMode.DEFAULT) {
  override val testName: String get() = getTestName(false)
}