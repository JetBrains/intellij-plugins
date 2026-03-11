// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.service.hybridmode

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.platform.lsp.api.LspServerManager
import com.intellij.platform.lsp.tests.waitForDiagnosticsFromLspServer
import org.jetbrains.vuejs.VueTsConfigFile
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.typescript.service.VueLanguageToolsVersion
import org.jetbrains.vuejs.lang.typescript.service.VueServiceRuntime
import org.jetbrains.vuejs.lang.typescript.service.lsp.VueLspServerHybridModeSupportProvider
import org.jetbrains.vuejs.lang.vueRelativeTestDataPath
import org.jetbrains.vuejs.options.VueLSMode
import org.jetbrains.vuejs.options.VueSettings

/**
 * Tests service lifecycle behavior: disabling the service clears LSP diagnostics.
 */
class VueHybridModeServiceLifecycleTest : VueHybridModeTestBase() {

  override fun getBasePath(): String =
    "${vueRelativeTestDataPath()}/service/hybrid/lifecycle"

  override fun setUp() {
    super.setUp()
    myFixture.configureVueDependencies(VueTestModule.VUE_3_5_0)
    myFixture.configureByText(VueTsConfigFile.FILE_NAME, VueTsConfigFile.DEFAULT_TSCONFIG_CONTENT)
  }

  fun `test LSP errors cleared when service disabled`() {
    myFixture.configureByFile("lspErrorCleared/App.vue")
    waitForDiagnosticsFromLspServer(project, file.virtualFile)
    val highlightingBefore = myFixture.doHighlighting()

    val vueErrors = highlightingBefore.filter {
      it.severity >= HighlightSeverity.WARNING
      && it.description?.startsWith("Vue:") == true
    }
    assertFalse(
      "Expected Vue LSP errors for v-else without v-if, but got none",
      vueErrors.isEmpty(),
    )

    VueSettings.instance(project).serviceType = VueLSMode.DISABLED
    for (version in VueLanguageToolsVersion.entries) {
      LspServerManager.getInstance(project)
        .stopAndRestartIfNeeded(VueLspServerHybridModeSupportProvider.getProviderClass(VueServiceRuntime.Bundled(version)))
    }
    LspServerManager.getInstance(project)
      .stopAndRestartIfNeeded(VueLspServerHybridModeSupportProvider.getProviderClass(VueServiceRuntime.Manual))
    val highlightingAfter = myFixture.doHighlighting()

    val vueErrorsAfter = highlightingAfter.filter {
      it.severity >= HighlightSeverity.WARNING
      && it.description?.startsWith("Vue:") == true
    }
    assertTrue(
      "Expected Vue LSP errors to be cleared after disabling, but got: ${vueErrorsAfter.map { it.description }}",
      vueErrorsAfter.isEmpty(),
    )
  }
}
