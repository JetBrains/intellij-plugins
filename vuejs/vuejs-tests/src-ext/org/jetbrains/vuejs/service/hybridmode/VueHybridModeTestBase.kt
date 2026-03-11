// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.service.hybridmode

import com.intellij.lang.javascript.library.typings.TypeScriptExternalDefinitionsRegistry
import com.intellij.lang.javascript.service.BaseLspTypeScriptServiceTest
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.library.download.TypeScriptDefinitionFilesDirectory
import com.intellij.lang.typescript.lsp.JSBundledServiceNodePackage
import com.intellij.platform.lsp.api.LspServerManager
import com.intellij.util.text.SemVer
import org.jetbrains.vuejs.lang.typescript.service.VueLanguageToolsVersion
import org.jetbrains.vuejs.lang.typescript.service.VueServiceRuntime
import org.jetbrains.vuejs.lang.typescript.service.lsp.VueLspServerHybridModeLoaderFactory
import org.jetbrains.vuejs.lang.typescript.service.lsp.VueLspServerHybridModeSupportProvider
import org.jetbrains.vuejs.lang.typescript.service.plugin.VuePluginTypeScriptService
import org.jetbrains.vuejs.lang.typescript.service.plugin.VueTSPluginLoaderFactory
import org.jetbrains.vuejs.lang.typescript.service.vueLspPackageName
import org.jetbrains.vuejs.lang.typescript.service.vueTSPluginPackageName
import org.jetbrains.vuejs.options.VueLSMode
import org.jetbrains.vuejs.options.VueSettings

/**
 * Base test class for Vue Hybrid Mode where the LSP server and TS plugin
 * work together via the tsserver bridge.
 *
 * Sets up both services: the LSP hybrid mode server and the TS plugin service.
 * The LSP server communicates with the TS plugin via [org.jetbrains.vuejs.lang.typescript.service.lsp.VueHybridModeLsp4jClient].
 */
abstract class VueHybridModeTestBase(
  protected val bundledVersion: VueLanguageToolsVersion = VueLanguageToolsVersion.DEFAULT,
) : BaseLspTypeScriptServiceTest() {

  override fun getExtension(): String =
    "vue"

  override fun setUp() {
    super.setUp()
    TypeScriptLanguageServiceUtil.setUseService(true)
    TypeScriptExternalDefinitionsRegistry.testTypingsRootPath =
      TypeScriptDefinitionFilesDirectory.getGlobalAutoDownloadTypesDirectoryPath()

    val vueSettings = VueSettings.instance(project)
    vueSettings.serviceType = VueLSMode.MANUAL
    vueSettings.manualSettings.mode = VueSettings.ManualMode.HYBRID_MODE

    val bundledRuntime = VueServiceRuntime.Bundled(bundledVersion)
    ensureServerDownloaded(VueLspServerHybridModeLoaderFactory.getLoader(bundledRuntime))
    assertNotNull(VueTSPluginLoaderFactory.getLoader(bundledRuntime).getAbsolutePath(this.project))

    vueSettings.manualSettings.lspHybridModePackage = JSBundledServiceNodePackage(
      packageName = vueLspPackageName,
      packageVersion = SemVer.parseFromText(bundledVersion.versionString),
    )
    vueSettings.manualSettings.tsPluginPackage = JSBundledServiceNodePackage(
      packageName = vueTSPluginPackageName,
      packageVersion = SemVer.parseFromText(bundledVersion.versionString),
    )
  }

  override fun tearDown() {
    try {
      TypeScriptLanguageServiceUtil.setUseService(false)
    }
    catch (e: Throwable) {
      addSuppressedException(e)
    }
    finally {
      super.tearDown()
    }
  }

  protected fun assertLspServiceActive() {
    val runtime = VueServiceRuntime.Manual
    val providerClass = VueLspServerHybridModeSupportProvider.getProviderClass(runtime)
    val servers = LspServerManager.getInstance(project).getServersForProvider(providerClass)
    assertFalse(
      "LSP hybrid mode server should be running for version $bundledVersion",
      servers.isEmpty(),
    )
  }

  protected fun findTsPluginService(): VuePluginTypeScriptService? {
    val runtime = VueServiceRuntime.Manual
    return VuePluginTypeScriptService.find(project, runtime)
  }

  protected fun assertTsPluginServiceActive() {
    val service = findTsPluginService()
                  ?: error("Vue TS plugin service should exist for runtime $bundledVersion")
    assertTrue("Vue TS plugin service should be created", service.isServiceCreated())
  }
}
