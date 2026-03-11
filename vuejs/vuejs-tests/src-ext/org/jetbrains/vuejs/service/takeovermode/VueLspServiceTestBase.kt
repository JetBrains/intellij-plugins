// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.service.takeovermode

import com.intellij.lang.javascript.library.typings.TypeScriptExternalDefinitionsRegistry
import com.intellij.lang.javascript.service.BaseLspTypeScriptServiceTest
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.library.download.TypeScriptDefinitionFilesDirectory
import com.intellij.util.text.SemVer
import org.jetbrains.vuejs.lang.typescript.service.VueServiceRuntime
import org.jetbrains.vuejs.lang.typescript.service.VueLanguageToolsVersion
import org.jetbrains.vuejs.lang.typescript.service.lsp.VueLspServerHybridModeLoaderFactory
import org.jetbrains.vuejs.lang.typescript.service.lsp.VueLspTakeoverModeTypeScriptService
import org.jetbrains.vuejs.options.VueLSMode
import org.jetbrains.vuejs.options.VueSettings

abstract class VueLspServiceTestBase : BaseLspTypeScriptServiceTest() {
  protected val tsconfig = """
    {
      "compilerOptions": {
        "strict": true
      }
    }
  """.trimIndent()

  override fun getExtension(): String = "vue"

  override fun setUp() {
    super.setUp()
    val vueSettings = VueSettings.instance(project)
    TypeScriptLanguageServiceUtil.setUseService(true)
    TypeScriptExternalDefinitionsRegistry.testTypingsRootPath = TypeScriptDefinitionFilesDirectory.getGlobalAutoDownloadTypesDirectoryPath()
    vueSettings.serviceType = VueLSMode.MANUAL
    vueSettings.manualSettings.mode = VueSettings.ManualMode.ONLY_LSP_SERVER

    val runtime = VueServiceRuntime.Bundled(VueLanguageToolsVersion.DEFAULT)
    ensureServerDownloaded(VueLspServerHybridModeLoaderFactory.getLoader(runtime))
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

  protected fun assertCorrectService(version: SemVer? = null) {
    assertCorrectServiceImpl<VueLspTakeoverModeTypeScriptService>(version)
  }
}