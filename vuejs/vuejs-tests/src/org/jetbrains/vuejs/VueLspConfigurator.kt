// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs

import com.intellij.polySymbols.testFramework.PolySymbolsTestConfigurator
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl

class VueLspConfigurator() :
  PolySymbolsTestConfigurator {

  override fun configure(
    fixture: CodeInsightTestFixture,
  ) {
    fixture as CodeInsightTestFixtureImpl
    fixture.canChangeDocumentDuringHighlighting(true)
  }
}
