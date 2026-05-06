// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs

import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.platform.lsp.tests.waitUntilFileOpenedByLspServer
import com.intellij.polySymbols.testFramework.PolySymbolsTestConfigurator
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl

class VueLspConfigurator :
  PolySymbolsTestConfigurator {

  override fun configure(
    fixture: CodeInsightTestFixture,
  ) {
    // TODO: check why it's required in hybrid mode
    TypeScriptLanguageServiceUtil.setUseService(true)

    fixture as CodeInsightTestFixtureImpl
    fixture.canChangeDocumentDuringHighlighting(true)
  }

  fun waitForLspServer(
    fixture: CodeInsightTestFixture,
  ) {
    val currentFile = fixture.file.virtualFile
    if (currentFile.extension == "vue") {
      waitUntilFileOpenedByLspServer(fixture.project, currentFile)
    }
  }
}
