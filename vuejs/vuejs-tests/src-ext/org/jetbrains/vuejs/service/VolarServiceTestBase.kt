// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.service

import com.intellij.javascript.debugger.com.intellij.lang.javascript.service.BaseLspTypeScriptServiceTest
import com.intellij.lang.javascript.library.typings.TypeScriptExternalDefinitionsRegistry
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.library.download.TypeScriptDefinitionFilesDirectory
import com.intellij.openapi.util.Disposer
import org.jetbrains.vuejs.lang.typescript.service.volar.VolarExecutableDownloader
import org.jetbrains.vuejs.lang.typescript.service.volar.VolarTypeScriptService
import org.jetbrains.vuejs.options.VueServiceSettings
import org.jetbrains.vuejs.options.getVueSettings

abstract class VolarServiceTestBase : BaseLspTypeScriptServiceTest() {
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
    val old = vueSettings.serviceType
    TypeScriptLanguageServiceUtil.setUseService(true)
    TypeScriptExternalDefinitionsRegistry.testTypingsRootPath = TypeScriptDefinitionFilesDirectory.getGlobalAutoDownloadTypesDirectoryPath()

    Disposer.register(testRootDisposable) {
      vueSettings.serviceType = old
      TypeScriptLanguageServiceUtil.setUseService(false)
    }
    vueSettings.serviceType = VueServiceSettings.VOLAR

    ensureServerDownloaded(VolarExecutableDownloader)
  }

  protected fun assertCorrectService() {
    assertCorrectServiceImpl<VolarTypeScriptService>()
  }
}