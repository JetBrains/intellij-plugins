// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.service.hybridmode

import com.intellij.lang.javascript.library.typings.TypeScriptExternalDefinitionsRegistry
import com.intellij.lang.javascript.service.BaseLspTypeScriptServiceTest
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.library.download.TypeScriptDefinitionFilesDirectory
import com.intellij.platform.lsp.api.LspServerManager
import org.jetbrains.vuejs.VueTsConfigFile
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.typescript.service.VueLanguageToolsVersion
import org.jetbrains.vuejs.lang.typescript.service.VueServiceRuntime
import org.jetbrains.vuejs.lang.typescript.service.lsp.VueLspServerHybridModeLoaderFactory
import org.jetbrains.vuejs.lang.typescript.service.lsp.VueLspServerHybridModeSupportProvider
import org.jetbrains.vuejs.lang.typescript.service.plugin.VuePluginTypeScriptService
import org.jetbrains.vuejs.lang.typescript.service.plugin.VueTSPluginLoaderFactory
import org.jetbrains.vuejs.options.VueLSMode
import org.jetbrains.vuejs.options.VueSettings
import java.io.File

/**
 * Verifies that all [VueLanguageToolsVersion] entries have their bundled resources present on disk
 * and that hybrid mode LSP servers can start for each version.
 */
class VueHybridModeBundledResourcesTest : BaseLspTypeScriptServiceTest() {

  override fun getExtension(): String =
    "vue"

  override fun setUp() {
    super.setUp()
    TypeScriptLanguageServiceUtil.setUseService(true)
    TypeScriptExternalDefinitionsRegistry.testTypingsRootPath =
      TypeScriptDefinitionFilesDirectory.getGlobalAutoDownloadTypesDirectoryPath()
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

  fun `test all bundled versions have LSP resources`() {
    for (version in VueLanguageToolsVersion.entries) {
      val runtime = VueServiceRuntime.Bundled(version)
      val loader = VueLspServerHybridModeLoaderFactory.getLoader(runtime)
      val path = loader.getAbsolutePath(project)
                 ?: error("LSP server path is null for $version")
      assertTrue(
        "LSP server files missing at $path for bundled version $version (version=${version.versionString})",
        File(path).exists(),
      )
    }
  }

  fun `test all bundled versions have TS plugin resources`() {
    for (version in VueLanguageToolsVersion.entries) {
      val runtime = VueServiceRuntime.Bundled(version)
      val loader = VueTSPluginLoaderFactory.getLoader(runtime)
      val path = loader.getAbsolutePath(project)
                 ?: error("TS plugin path is null for $version")
      assertTrue(
        "TS plugin files missing at $path for bundled version $version (version=${version.versionString})",
        File(path).exists(),
      )
    }
  }

  fun `test default version hybrid mode service starts`() {
    doTestHybridModeServiceStarts(
      VueLanguageToolsVersion.DEFAULT,
      VueTestModule.VUE_3_5_0,
    )
  }

  fun `test legacy version hybrid mode service starts`() {
    doTestHybridModeServiceStarts(
      VueLanguageToolsVersion.LEGACY,
      VueTestModule.VUE_2_7_14,
    )
  }

  private fun doTestHybridModeServiceStarts(
    version: VueLanguageToolsVersion,
    vueModule: VueTestModule,
  ) {
    val bundledRuntime = VueServiceRuntime.Bundled(version)
    val manualRuntime = VueServiceRuntime.Manual
    ensureServerDownloaded(VueLspServerHybridModeLoaderFactory.getLoader(bundledRuntime))

    val vueSettings = VueSettings.instance(project)
    vueSettings.serviceType = VueLSMode.MANUAL
    vueSettings.manualSettings.mode = VueSettings.ManualMode.HYBRID_MODE
    myFixture.configureVueDependencies(vueModule)
    myFixture.configureByText(VueTsConfigFile.FILE_NAME, VueTsConfigFile.DEFAULT_TSCONFIG_CONTENT)
    myFixture.configureByText("App.vue", SIMPLE_VUE_FILE)

    myFixture.doHighlighting()

    val providerClass = VueLspServerHybridModeSupportProvider.getProviderClass(manualRuntime)
    val servers = LspServerManager.getInstance(project).getServersForProvider(providerClass)
    assertFalse(
      "LSP hybrid mode server (manual) should be running for version $version",
      servers.isEmpty(),
    )

    val tsPluginService = VuePluginTypeScriptService.find(project, manualRuntime)
                          ?: error("Vue TS plugin service (manual) should be running for version $version")
    assertTrue(
      "Vue TS plugin service should be created for version $version",
      tsPluginService.isServiceCreated(),
    )
  }

  companion object {
    @Suppress("HtmlUnknownAttribute")
    // language=vue
    private val SIMPLE_VUE_FILE = """
      <script setup lang="ts">
      const msg: string = 'hello'
      </script>
    """.trimIndent()
  }
}
