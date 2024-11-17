// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.service

import com.intellij.lang.javascript.library.typings.TypeScriptExternalDefinitionsRegistry
import com.intellij.lang.javascript.service.BaseLspTypeScriptServiceTest
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.library.download.TypeScriptDefinitionFilesDirectory
import com.intellij.util.text.SemVer
import org.jetbrains.vuejs.lang.typescript.service.VueLspServerLoader
import org.jetbrains.vuejs.lang.typescript.service.lsp.VueLspTypeScriptService
import org.jetbrains.vuejs.options.VueServiceSettings
import org.jetbrains.vuejs.options.getVueSettings

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
    val vueSettings = getVueSettings(project)
    TypeScriptLanguageServiceUtil.setUseService(true)
    TypeScriptExternalDefinitionsRegistry.testTypingsRootPath = TypeScriptDefinitionFilesDirectory.getGlobalAutoDownloadTypesDirectoryPath()
    vueSettings.serviceType = VueServiceSettings.VOLAR

    ensureServerDownloaded(VueLspServerLoader)
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
    assertCorrectServiceImpl<VueLspTypeScriptService>(version)
  }
}