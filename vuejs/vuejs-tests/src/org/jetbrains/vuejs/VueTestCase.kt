// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs

import com.intellij.injected.editor.DocumentWindow
import com.intellij.javascript.testFramework.web.WebFrameworkTestCase
import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.lang.javascript.waitEmptyServiceQueueForService
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerServiceImpl
import com.intellij.lang.typescript.lsp.TypeScriptGoLspService
import com.intellij.lang.typescript.tsc.TypeScriptGoTypeEvaluatorMode
import com.intellij.lang.typescript.tsc.TypeScriptServiceTestMixin
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.platform.lsp.testFramework.waitForDiagnosticsFromLspServer
import com.intellij.polySymbols.testFramework.HybridTestMode
import com.intellij.polySymbols.testFramework.PolySymbolsTestConfigurator
import com.intellij.polySymbols.testFramework.disableAstLoadingFilter
import com.intellij.testFramework.ExpectedHighlightingData
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.intellij.testFramework.runInEdtAndWait
import org.jetbrains.vuejs.VueLspUtils.waitTypeScriptGoLspServerInit
import org.jetbrains.vuejs.index.VUE_MODULE
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.getVueTestDataPath
import org.jetbrains.vuejs.lang.typescript.service.VueLanguageToolsVersion
import org.jetbrains.vuejs.lang.typescript.service.VueServiceRuntime
import org.jetbrains.vuejs.lang.typescript.service.VueServiceTestMixin.setForceLegacyPluginUsage
import org.jetbrains.vuejs.lang.typescript.service.plugin.VuePluginTypeScriptService
import org.junit.ComparisonFailure

enum class VueTestMode {
  DEFAULT,
  LEGACY_PLUGIN,
  TS_GO_PROXY,
  NO_PLUGIN,

  ;
}

abstract class VueTestCase(
  override val testCasePath: String,
  private val testMode: VueTestMode = VueTestMode.DEFAULT,
) : WebFrameworkTestCase(
  mode = if (testMode != VueTestMode.NO_PLUGIN) HybridTestMode.CodeInsightFixture else HybridTestMode.BasePlatform,
) {

  override fun adjustConfigurators(
    configurators: List<PolySymbolsTestConfigurator>,
  ): List<PolySymbolsTestConfigurator> =
    buildList {
      addAll(super.adjustConfigurators(configurators))

      add(VueToolsConfigurator(testMode))

      if (none { it is VueTsConfigFile }) {
        add(VueTsConfigFile())
      }
    }

  override fun adjustModules(
    modules: Array<out WebFrameworkTestModule>,
  ): Array<out WebFrameworkTestModule> =
    buildList {
      addAll(modules)

      if (modules.none { VUE_MODULE in it.packageNames })
        add(VueTestModule.VUE_3_5_0)

      add(VueTestModule.VUE_TSCONFIG_0_9_1)
    }.toTypedArray()

  private fun TestConfiguration.getVueLanguageToolsVersion(): VueLanguageToolsVersion? =
    configurators
      .filterIsInstance<VueToolsConfigurator>()
      .single()
      .getBundledVersion()

  override fun beforeConfiguredTest(configuration: TestConfiguration) {
    configureVueSettings(project, testRootDisposable, testMode)

    when (testMode) {
      VueTestMode.DEFAULT,
      VueTestMode.LEGACY_PLUGIN,
        -> {
        val bundledVersion = configuration.getVueLanguageToolsVersion()!!
        setForceLegacyPluginUsage(bundledVersion == VueLanguageToolsVersion.LEGACY, testRootDisposable)

        val service = TypeScriptServiceTestMixin.setUpTypeScriptService(myFixture) {
          it is VuePluginTypeScriptService
          && it.runtime == VueServiceRuntime.Bundled(bundledVersion)
        } as TypeScriptServerServiceImpl

        service.assertProcessStarted()
        runInEdtAndWait {
          waitEmptyServiceQueueForService(service)
        }
      }

      VueTestMode.TS_GO_PROXY -> {
        disableAstLoadingFilter()

        TypeScriptServiceTestMixin.setUpTypeScriptService(
          fixture = myFixture,
          tsGoTypeEvaluatorMode = TypeScriptGoTypeEvaluatorMode.PROXY,
        ) {
          it::class == TypeScriptGoLspService::class
        }

        waitTypeScriptGoLspServerInit(project)
      }

      VueTestMode.NO_PLUGIN -> return
    }

    if (configuration.configurators.any { it is VueTsConfigFile && it.checkUsage }) {
      TypeScriptServerServiceImpl.requireTSConfigsForTypeEvaluation(
        testRootDisposable,
        myFixture.tempDirFixture.getFile(VueTsConfigFile.FILE_NAME)!!,
      )
    }

    configuration.configurators
      .filterIsInstance<VueLspConfigurator>()
      .forEach { it.waitForLspServer(myFixture) }

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

  protected fun disableAstLoadingFilterWhenPluginUsed() {
    if (testMode != VueTestMode.NO_PLUGIN)
      disableAstLoadingFilter()
  }

  override fun CodeInsightTestFixture.runHighlightingCheck(
    checkWarnings: Boolean,
    checkInformation: Boolean,
    checkWeakWarnings: Boolean,
  ) {
    if (testMode == VueTestMode.NO_PLUGIN || file.virtualFile.extension != "vue") {
      checkHighlighting(checkWarnings, checkInformation, checkWeakWarnings)
      return
    }
    val document = editor.document.let { (it as? DocumentWindow)?.delegate ?: it }
    val data = ExpectedHighlightingData(document, checkWarnings, checkWeakWarnings, checkInformation)
    data.init()
    collectAndCheckExpectedHighlighting(data)
  }

  /**
   * The LSP server serves the diagnostics asynchronously, so a single highlighting pass can run
   * before the pull response arrives. A failed check waits for the next diagnostics from the
   * server and runs again. The last attempt runs even after a wait timeout, because a response
   * can land between a failed check and the start of the wait.
   */
  override fun CodeInsightTestFixture.collectAndCheckExpectedHighlighting(data: ExpectedHighlightingData) {
    val maxAttempts = if (testMode == VueTestMode.NO_PLUGIN) 1 else 5
    var lastFailure: ComparisonFailure? = null
    for (attempt in 1..maxAttempts) {
      if (attempt > 1) {
        try {
          // The tsserver warm-up can hold the diagnostics back for many seconds on a loaded machine.
          waitForDiagnosticsFromLspServer(project, file.virtualFile, timeout = 10)
        }
        catch (_: AssertionError) {
          // No new diagnostics arrived in time. The next attempt checks the current state anyway.
        }
      }
      try {
        (this as CodeInsightTestFixtureImpl).collectAndCheckHighlighting(data)
        return
      }
      catch (failure: ComparisonFailure) {
        lastFailure = failure
      }
    }
    throw lastFailure!!
  }
}
