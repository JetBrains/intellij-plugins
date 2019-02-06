// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.language.parser

import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.mock.MockApplicationEx
import com.intellij.mock.MockFileTypeManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.extensions.ExtensionPoint
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.util.Getter
import com.intellij.psi.codeStyle.*
import com.intellij.psi.css.impl.util.scheme.CssElementDescriptorFactory2
import com.intellij.psi.impl.source.codeStyle.PersistableCodeStyleSchemes
import com.intellij.testFramework.MockSchemeManagerFactory
import org.jetbrains.vuejs.VueFileType

class VueHighlightingLexerTest : VueLexerTest() {
  override fun setUp() {
    super.setUp()
    val instance = MockApplicationEx(testRootDisposable)
    ApplicationManager.setApplication(instance,
                                      Getter { FileTypeManager.getInstance() },
                                      testRootDisposable)
    instance.registerService(CssElementDescriptorFactory2::class.java, CssElementDescriptorFactory2(null))
    instance.registerService(FileTypeManager::class.java, MockFileTypeManager(VueFileType.INSTANCE))
    registerCodeStyle(instance)
  }

  private fun registerCodeStyle(instance: MockApplicationEx) {
    instance.registerService(AppCodeStyleSettingsManager::class.java, AppCodeStyleSettingsManager())

    val area = Extensions.getRootArea()
    if (!area.hasExtensionPoint(CodeStyleSettingsProvider.EXTENSION_POINT_NAME)) {
      area.registerExtensionPoint(CodeStyleSettingsProvider.EXTENSION_POINT_NAME, CodeStyleSettingsProvider::class.java.name, ExtensionPoint.Kind.INTERFACE, testRootDisposable)
    }

    if (!area.hasExtensionPoint(LanguageCodeStyleSettingsProvider.EP_NAME)) {
      area.registerExtensionPoint(LanguageCodeStyleSettingsProvider.EP_NAME, LanguageCodeStyleSettingsProvider::class.java.name, ExtensionPoint.Kind.INTERFACE, testRootDisposable)
    }

    val settings = CodeStyleSettings()
    instance.registerService<CodeStyleSchemes>(CodeStyleSchemes::class.java,
                                               PersistableCodeStyleSchemes(MockSchemeManagerFactory()))
    CodeStyleSettingsManager.getInstance().setTemporarySettings(settings)
  }

  override fun tearDown() {
    try {
      CodeStyleSettingsManager.getInstance().dropTemporarySettings()
    } finally {
      super.tearDown()
    }
  }

  fun testScriptES6() = doFileTest("vue")
  fun testTemplateHtml() = doFileTest("vue")

  override fun createLexer() = org.jetbrains.vuejs.language.VueHighlightingLexer(JSLanguageLevel.ES6)
  override fun getDirPath() = "/contrib/vuejs/vuejs-tests/testData/highlightingLexer"
}