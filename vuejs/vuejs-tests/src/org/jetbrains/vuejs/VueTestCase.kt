// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs

import com.intellij.javascript.testFramework.web.WebFrameworkTestCase
import com.intellij.lang.javascript.HybridTestMode
import com.intellij.lang.javascript.waitEmptyServiceQueueForService
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerServiceImpl
import com.intellij.lang.typescript.tsc.TypeScriptServiceTestMixin
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.testFramework.runInEdtAndWait
import org.jetbrains.vuejs.lang.getVueTestDataPath
import org.jetbrains.vuejs.lang.typescript.service.VuePluginTypeScriptService

abstract class VueTestCase(
  override val testCasePath: String,
  private val useTsc: Boolean = false,
) : WebFrameworkTestCase(if (useTsc) HybridTestMode.CodeInsightFixture else HybridTestMode.BasePlatform) {

  override fun beforeConfiguredTest(configuration: TestConfiguration) {
    if (useTsc) {
      val service = TypeScriptServiceTestMixin.setUpTypeScriptService(myFixture) {
        it::class == VuePluginTypeScriptService::class
      } as TypeScriptServerServiceImpl
      
      service.assertProcessStarted()
      runInEdtAndWait {
        waitEmptyServiceQueueForService(service)
      }

      // TODO: support config
      /*
      if (configuration.configurators.any { it is VueTsConfigFile }) {
        TypeScriptServerServiceImpl.requireTSConfigsForTypeEvaluation(testRootDisposable, myFixture.tempDirFixture.getFile("tsconfig.json")!!)
      }
      */
      runInEdtAndWait {
        FileDocumentManager.getInstance().saveAllDocuments()
      }
    }
  }


  override val testDataRoot: String
    get() = getVueTestDataPath()

  override val defaultExtension: String
    get() = "vue"

  override val defaultDependencies: Map<String, String>
    get() = mapOf()
}