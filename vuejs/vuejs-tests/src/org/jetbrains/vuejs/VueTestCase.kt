// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs

import com.intellij.javascript.testFramework.web.WebFrameworkTestCase
import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.lang.javascript.HybridTestMode
import com.intellij.lang.javascript.waitEmptyServiceQueueForService
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerServiceImpl
import com.intellij.lang.typescript.tsc.TypeScriptServiceTestMixin
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.testFramework.runInEdtAndWait
import org.jetbrains.vuejs.index.VUE_MODULE
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.getVueTestDataPath
import org.jetbrains.vuejs.lang.typescript.service.VueServiceTestMixin.setForceLegacyPluginUsage
import org.jetbrains.vuejs.lang.typescript.service.VueTSPluginVersion
import org.jetbrains.vuejs.lang.typescript.service.plugin.VuePluginTypeScriptServiceBundled

enum class VueTestMode {
  DEFAULT,
  LEGACY_PLUGIN,
  NO_PLUGIN,

  ;
}

abstract class VueTestCase(
  override val testCasePath: String,
  private val testMode: VueTestMode = VueTestMode.DEFAULT,
) : WebFrameworkTestCase(
  mode = if (testMode != VueTestMode.NO_PLUGIN) HybridTestMode.CodeInsightFixture else HybridTestMode.BasePlatform,
) {

  override fun adjustModules(
    modules: Array<out WebFrameworkTestModule>,
  ): Array<out WebFrameworkTestModule> =
    buildList {
      addAll(modules)

      if (modules.none { VUE_MODULE in it.packageNames })
        add(VueTestModule.VUE_3_5_0)

      add(VueTestModule.VUE_TSCONFIG_0_9_1)
    }.toTypedArray()

  override fun beforeConfiguredTest(configuration: TestConfiguration) {
    val tsPluginVersion = getRequiredTypescriptPluginVersion(myFixture, testMode)
                          ?: return

    setForceLegacyPluginUsage(tsPluginVersion == VueTSPluginVersion.LEGACY, testRootDisposable)

    val service = TypeScriptServiceTestMixin.setUpTypeScriptService(myFixture) {
      it is VuePluginTypeScriptServiceBundled
      && it.version == tsPluginVersion
    } as TypeScriptServerServiceImpl

    service.assertProcessStarted()
    runInEdtAndWait {
      waitEmptyServiceQueueForService(service)
    }

    if (configuration.configurators.any { it is VueTsConfigFile }) {
      TypeScriptServerServiceImpl.requireTSConfigsForTypeEvaluation(
        testRootDisposable,
        myFixture.tempDirFixture.getFile(VueTsConfigFile.FILE_NAME)!!,
      )
    }

    runInEdtAndWait {
      FileDocumentManager.getInstance().saveAllDocuments()
    }
  }

  override val testDataRoot: String
    get() = getVueTestDataPath()

  override val defaultExtension: String
    get() = "vue"

  override val defaultDependencies: Map<String, String>
    get() = mapOf()
}