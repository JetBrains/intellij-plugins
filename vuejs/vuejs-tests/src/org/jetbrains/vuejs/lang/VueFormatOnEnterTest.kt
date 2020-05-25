// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.javascript.JSBaseEditorTestCase
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.openapi.application.PathManager
import com.jetbrains.plugins.jade.JadeLanguage
import org.jetbrains.plugins.sass.SASSLanguage
import org.jetbrains.plugins.stylus.StylusLanguage
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.jetbrains.vuejs.lang.html.psi.formatter.VueCodeStyleSettings

class VueFormatOnEnterTest: JSBaseEditorTestCase() {

  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/formatter/onEnter"

  fun testStylusEnterEnd() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) { styleSettings ->
      styleSettings.getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = false
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "script"
      }
      styleSettings.getLanguageIndentOptions(StylusLanguage.INSTANCE).INDENT_SIZE = 1
      styleSettings.getLanguageIndentOptions(VueLanguage.INSTANCE).INDENT_SIZE = 5
      styleSettings.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 5
      doEnterTestForExtension("vue")
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
      doEnterTestForExtension("vue")
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
      doEnterTestForExtension("vue")
    }
  }

  fun testJSEnterStart() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) { styleSettings ->
      styleSettings.getCustomSettings(VueCodeStyleSettings::class.java).let {
        it.UNIFORM_INDENT = false
        it.INDENT_CHILDREN_OF_TOP_LEVEL = "script"
      }
      styleSettings.getLanguageIndentOptions(JavascriptLanguage.INSTANCE).INDENT_SIZE = 2
      styleSettings.getLanguageIndentOptions(VueLanguage.INSTANCE).INDENT_SIZE = 6
      styleSettings.getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 6
      doEnterTestForExtension("vue")
    }
  }
}