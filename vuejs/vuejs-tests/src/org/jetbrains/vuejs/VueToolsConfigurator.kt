// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs

import com.intellij.polySymbols.testFramework.PolySymbolsTestConfigurator
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.jetbrains.vuejs.lang.typescript.service.VueLanguageToolsVersion
import org.jetbrains.vuejs.lang.typescript.service.VueServiceTestMixin.setForceLegacyPluginUsage

class VueToolsConfigurator(
  private val testMode: VueTestMode,
) : PolySymbolsTestConfigurator {

  private var configured: Boolean = false
  private var bundledVersion: VueLanguageToolsVersion? = null

  override fun configure(
    fixture: CodeInsightTestFixture,
  ) {
    require(!configured) {
      "Already configured"
    }

    configured = true
    bundledVersion = getRequiredHybridModeBundledVersion(fixture, testMode)
                     ?: return

    setForceLegacyPluginUsage(
      bundledVersion == VueLanguageToolsVersion.LEGACY,
      fixture.testRootDisposable,
    )
  }

  fun getBundledVersion(): VueLanguageToolsVersion? {
    require(configured)

    return bundledVersion
  }
}
