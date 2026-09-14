// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.service.hybridmode

import com.intellij.lang.javascript.library.typings.TypeScriptExternalDefinitionsRegistry
import com.intellij.lang.javascript.service.BaseLspTypeScriptServiceTest
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.library.download.TypeScriptDefinitionFilesDirectory
import org.jetbrains.vuejs.lang.typescript.service.VueLanguageToolsVersion
import org.jetbrains.vuejs.lang.typescript.service.VueServiceRuntime
import org.jetbrains.vuejs.lang.typescript.service.lsp.VueLspServerHybridModeLoaderFactory
import org.jetbrains.vuejs.lang.typescript.service.plugin.VueTSPluginLoaderFactory
import java.io.File

/**
 * Verifies that all [VueLanguageToolsVersion] entries have their bundled resources present on disk.
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
}
