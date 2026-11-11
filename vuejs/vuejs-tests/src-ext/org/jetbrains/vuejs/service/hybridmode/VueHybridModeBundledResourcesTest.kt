// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.service.hybridmode

import com.intellij.lang.javascript.library.typings.TypeScriptExternalDefinitionsRegistry
import com.intellij.lang.javascript.modules.JSTempDirFixtureOptions
import com.intellij.lang.javascript.service.LspTypeScriptServiceFixture.Companion.lspTypeScriptServiceFixture
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.library.download.TypeScriptDefinitionFilesDirectory
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.vuejs.lang.typescript.service.VueLanguageToolsVersion
import org.jetbrains.vuejs.lang.typescript.service.VueServiceRuntime
import org.jetbrains.vuejs.lang.typescript.service.lsp.VueLspServerHybridModeLoaderFactory
import org.jetbrains.vuejs.lang.typescript.service.plugin.VueTSPluginLoaderFactory
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Verifies that all [VueLanguageToolsVersion] entries have their bundled resources present on disk.
 */
@TestApplication
class VueHybridModeBundledResourcesTest {
  private val vue by lspTypeScriptServiceFixture(JSTempDirFixtureOptions.forBasePath("").withExtension("vue"))

  @BeforeEach
  fun setUp() {
    Disposer.register(vue.testRootDisposable) { TypeScriptLanguageServiceUtil.setUseService(false) }
    TypeScriptLanguageServiceUtil.setUseService(true)
    TypeScriptExternalDefinitionsRegistry.testTypingsRootPath =
      TypeScriptDefinitionFilesDirectory.getGlobalAutoDownloadTypesDirectoryPath()
  }

  @Test
  fun `test all bundled versions have LSP resources`() {
    for (version in VueLanguageToolsVersion.entries) {
      val runtime = VueServiceRuntime(version)
      val loader = VueLspServerHybridModeLoaderFactory.getLoader(runtime)
      val path = loader.getAbsolutePath(vue.project)
                 ?: error("LSP server path is null for $version")
      assertTrue(
        File(path).exists(),
        "LSP server files missing at $path for bundled version $version (version=${version.versionString})",
      )
    }
  }

  @Test
  fun `test all bundled versions have TS plugin resources`() {
    for (version in VueLanguageToolsVersion.entries) {
      val runtime = VueServiceRuntime(version)
      val loader = VueTSPluginLoaderFactory.getLoader(runtime)
      val path = loader.getAbsolutePath(vue.project)
                 ?: error("TS plugin path is null for $version")
      assertTrue(
        File(path).exists(),
        "TS plugin files missing at $path for bundled version $version (version=${version.versionString})",
      )
    }
  }
}
