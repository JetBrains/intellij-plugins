// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.service

import com.intellij.lang.javascript.library.typings.TypeScriptExternalDefinitionsRegistry
import com.intellij.lang.javascript.modules.JSTempDirWithNodeInterpreterTest
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.library.download.TypeScriptDefinitionFilesDirectory
import com.intellij.platform.lsp.tests.checkLspHighlighting
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import junit.framework.TestCase
import org.jetbrains.vuejs.lang.VueInspectionsProvider
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.typescript.service.volar.VolarExecutableDownloader
import org.jetbrains.vuejs.lang.typescript.service.volar.VolarTypeScriptService
import org.jetbrains.vuejs.options.VueServiceSettings
import org.jetbrains.vuejs.options.getVueSettings
import org.junit.Test

class VolarServiceTest : JSTempDirWithNodeInterpreterTest() {

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
    (myFixture as CodeInsightTestFixtureImpl).canChangeDocumentDuringHighlighting(true)

    VolarExecutableDownloader.getExecutableOrRefresh(project) // could run blocking download
    TestCase.assertNotNull(VolarExecutableDownloader.getExecutable())
  }

  @Test
  fun testSimpleVue() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)

    myFixture.configureByText("tsconfig.json", "{}")
    myFixture.configureByText("Simple.vue", """
      <script setup lang="ts">
      let <error descr="Volar: Type 'number' is not assignable to type 'string'.">a</error>:string = 1; 
      </script>
      
      <template><div></div></template>
    """)
    myFixture.doHighlighting()
    val service = TypeScriptService.getForFile(project, file.virtualFile)
    UsefulTestCase.assertInstanceOf(service, VolarTypeScriptService::class.java)
    TestCase.assertTrue(service!!.isServiceCreated())
    myFixture.checkLspHighlighting()
  }
}