// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs

import com.intellij.injected.editor.DocumentWindow
import com.intellij.javascript.testFramework.web.WebFrameworkFixture
import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.lang.javascript.waitEmptyServiceQueueForService
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerServiceImpl
import com.intellij.lang.typescript.lsp.TypeScriptGoLspService
import com.intellij.lang.typescript.tsc.TypeScriptServiceTestMixin
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VirtualFileFilter
import com.intellij.platform.lsp.testFramework.waitForDiagnosticsFromLspServer
import com.intellij.polySymbols.testFramework.HybridFixtureContext
import com.intellij.polySymbols.testFramework.HybridTestMode
import com.intellij.polySymbols.testFramework.PolySymbolsTestConfigurator
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.testFramework.ExpectedHighlightingData
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.intellij.testFramework.junit5.fixture.TestFixture
import com.intellij.testFramework.runInEdtAndWait
import org.jetbrains.annotations.TestOnly
import org.jetbrains.vuejs.VueLspUtils.waitTypeScriptGoLspServerInit
import org.jetbrains.vuejs.index.VUE_MODULE
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.getVueTestDataPath
import org.jetbrains.vuejs.lang.typescript.service.VueLanguageToolsVersion
import org.jetbrains.vuejs.lang.typescript.service.VueServiceRuntime
import org.jetbrains.vuejs.lang.typescript.service.plugin.VuePluginTypeScriptService

enum class VueTestMode {
  DEFAULT,
  TS_GO_PROXY,
  NO_PLUGIN,

  ;
}

/**
 * Creates a [VueFixture] for the Vue code insight tests. Declare the result as an instance property.
 * Each test gets a new project. Without [VueTestMode.NO_PLUGIN], the project is a heavy project with the TypeScript service.
 */
@TestOnly
fun vueFixture(
  testCasePath: String,
  testMode: VueTestMode = VueTestMode.DEFAULT,
  dirModeByDefault: Boolean = false,
): TestFixture<VueFixture> =
  vueFixture(testMode) { VueFixture(it, testCasePath, testMode, dirModeByDefault) }

/**
 * Creates a sub-fixture of [VueFixture] with [create]. [testMode] selects the project of the fixture.
 */
@TestOnly
fun <T : VueFixture> vueFixture(
  testMode: VueTestMode,
  create: (context: HybridFixtureContext) -> T,
): TestFixture<T> =
  hybridFixture(
    { if (testMode != VueTestMode.NO_PLUGIN) HybridTestMode.CodeInsightFixture else HybridTestMode.BasePlatform },
    create,
  )

/**
 * The fixture of the Vue code insight tests: a [WebFrameworkFixture] with the Vue test data root and the `vue` extension.
 * The fixture adds the [VueToolsConfigurator] and a [VueTsConfigFile], adds the Vue modules,
 * and starts the TypeScript service of [testMode] before each configured test.
 * Call a member from a background thread.
 */
open class VueFixture(
  context: HybridFixtureContext,
  testCasePath: String,
  val testMode: VueTestMode,
  dirModeByDefault: Boolean = false,
) : WebFrameworkFixture(
  context,
  testDataRoot = getVueTestDataPath(),
  testCasePath = testCasePath,
  defaultExtension = "vue",
  defaultDependencies = mapOf(),
  dirModeByDefault = dirModeByDefault,
) {

  /**
   * The filter of the directory comparison. A test reads it to visit the same files.
   */
  public override val directoriesCompareFileFilter: VirtualFileFilter
    get() = super.directoriesCompareFileFilter

  override fun adjustConfigurators(
    configurators: List<PolySymbolsTestConfigurator>,
  ): List<PolySymbolsTestConfigurator> =
    buildList {
      add(VueSettingsConfigurator())

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

  override fun setUp() {
    super.setUp()
    if (testMode != VueTestMode.NO_PLUGIN) {
      // The test runs off the EDT, so a roots change can look up the TypeScript service before its setup, or when the module is disposed.
      // The test interpreter lives as long as the project, so the lookup does not find a global TypeScript package.
      TypeScriptServiceTestMixin.configureNodeInterpreter(fixture, disposable = fixture.projectDisposable)
    }
  }

  /**
   * Applies the Vue settings of [testMode] before the configured file opens in the editor.
   * The test runs off the EDT, so the editor listeners can send a request to a TypeScript service as soon as the file opens.
   * With the settings of [testMode], the request goes to the service of [testMode].
   */
  private inner class VueSettingsConfigurator : PolySymbolsTestConfigurator {
    override fun configure(fixture: CodeInsightTestFixture) {
      configureVueSettings(project, testRootDisposable, testMode)
    }
  }

  private fun TestConfiguration.getVueLanguageToolsVersion(): VueLanguageToolsVersion? =
    configurators
      .filterIsInstance<VueToolsConfigurator>()
      .single()
      .getBundledVersion()

  override fun beforeConfiguredTest(configuration: TestConfiguration) {
    // The configuration of the project can schedule a restart of the language services on the EDT.
    // The restart runs before the setup of the service, as in a test that runs on the EDT.
    runInEdtAndWait {
      PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
    }

    when (testMode) {
      VueTestMode.DEFAULT -> {
        val bundledVersion = configuration.getVueLanguageToolsVersion()!!

        val service = TypeScriptServiceTestMixin.setUpTypeScriptService(fixture) {
          it is VuePluginTypeScriptService
          && it.runtime == VueServiceRuntime(bundledVersion)
        } as TypeScriptServerServiceImpl

        service.assertProcessStarted()
        runInEdtAndWait {
          waitEmptyServiceQueueForService(service)
        }
      }

      VueTestMode.TS_GO_PROXY -> {
        disableAstLoadingFilter()

        TypeScriptServiceTestMixin.setUpTypeScriptService(fixture = fixture) {
          it::class == TypeScriptGoLspService::class
        }

        waitTypeScriptGoLspServerInit(project)
      }

      VueTestMode.NO_PLUGIN -> return
    }

    if (configuration.configurators.any { it is VueTsConfigFile && it.checkUsage }) {
      TypeScriptServerServiceImpl.requireTSConfigsForTypeEvaluation(
        testRootDisposable,
        fixture.tempDirFixture.getFile(VueTsConfigFile.FILE_NAME)!!,
      )
    }

    // The wait for the language server dispatches the events of the EDT.
    runInEdtAndWait {
      configuration.configurators
        .filterIsInstance<VueLspConfigurator>()
        .forEach { it.waitForLspServer(fixture) }

      FileDocumentManager.getInstance().saveAllDocuments()
    }
  }

  /**
   * Disables the AST loading filter, unless the test runs with [VueTestMode.NO_PLUGIN]. Call it from any thread.
   */
  fun disableAstLoadingFilterWhenPluginUsed() {
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
    var lastFailure: AssertionError? = null
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
      catch (failure: AssertionError) {
        // A file with a test data path fails with FileComparisonFailedError, which is not a ComparisonFailure.
        lastFailure = failure
      }
    }
    throw lastFailure!!
  }
}
