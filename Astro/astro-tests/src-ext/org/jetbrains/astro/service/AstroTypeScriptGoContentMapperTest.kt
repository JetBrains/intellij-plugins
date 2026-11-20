// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.service

import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.lang.typescript.lsp.TypeScriptGoLspService
import com.intellij.lang.typescript.tsc.TypeScriptServiceTestMixin
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspClientManager
import com.intellij.platform.lsp.testFramework.waitForDiagnosticsFromLspServer
import com.intellij.platform.lsp.testFramework.waitUntilFileOpenedByLspServer
import com.intellij.polySymbols.testFramework.HybridTestMode
import com.intellij.polySymbols.testFramework.PolySymbolsTestConfigurator
import com.intellij.polySymbols.testFramework.checkGotoDeclaration
import com.intellij.polySymbols.testFramework.disableAstLoadingFilter
import com.intellij.testFramework.ExpectedHighlightingData
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.VfsTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.intellij.testFramework.runInEdtAndWait
import org.jetbrains.astro.AstroCodeInsightTestCase
import org.jetbrains.astro.AstroTestMode
import org.jetbrains.astro.AstroTestModule
import org.jetbrains.astro.AstroTypeScriptGoConfigurator
import org.jetbrains.astro.copyBundledAstroContentMapper
import org.jetbrains.astro.service.settings.AstroServiceMode
import org.jetbrains.astro.service.settings.getAstroServiceSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Uses TypeScript 7 and keeps the test project files intact.
 * The test installs the `astro` package, and it starts the TypeScript 7 service before each configured test.
 */
@RunWith(JUnit4::class)
class AstroTypeScriptGoContentMapperTest :
  AstroCodeInsightTestCase("typescriptGo/contentMapper", AstroTestMode.NO_SERVICE, HybridTestMode.CodeInsightFixture) {

  override val dirModeByDefault: Boolean = true

  @Test
  fun testNoTsconfig() {
    doContentMapperTest(expectedHighlightingPaths = listOf("Card.astro", "main.ts")) {
      checkFileHighlighting("Card.astro")
      checkFileHighlighting("main.ts")
      assertEquals(AstroServiceMode.ENABLED, getAstroServiceSettings(project).serviceMode)
      assertFalse(runReadAction { AstroActivationHelper.isEnabledInSettings(project) })
      assertTrue(LspClientManager.getInstance(project).getClients(AstroLspIntegrationProvider::class.java).isEmpty())
    }
  }

  @Test
  fun testTsconfigWithAstroContentMapper() {
    checkHighlighting("Card.astro", "main.ts", astroContentMapper = true)
  }

  @Test
  fun testAstroFileOutsideTsconfig() {
    checkHighlighting("src/main.ts", "other/Other.astro", astroContentMapper = true)
  }

  @Test
  fun testGotoDeclaration() {
    doContentMapperTest(dirName = "noTsconfig") {
      waitUntilOpenedByServer(configureFromTempProjectFile("main.ts").virtualFile)
      checkGotoDeclaration("import Card from \"./<caret>Card.astro\"", "<caret>---", "Card.astro")
      waitUntilOpenedByServer(configureFromTempProjectFile("Card.astro").virtualFile)
      checkGotoDeclaration("<span>{<caret>double(", "export function <caret>double", "utils.ts")
    }
  }

  @Test
  fun testAstroInNodeModules() {
    checkHighlighting("main.ts", "node_modules/astro-library/Widget.astro")
  }

  private var expectedDataConfigurator: ExpectedDataConfigurator? = null
  private var currentDirName: String? = null

  override fun adjustModules(modules: Array<out WebFrameworkTestModule>): Array<out WebFrameworkTestModule> =
    if (modules.contains(AstroTestModule.ASTRO_5_14_4)) modules else arrayOf(*modules, AstroTestModule.ASTRO_5_14_4)

  override fun beforeConfiguredTest(configuration: TestConfiguration) {
    super.beforeConfiguredTest(configuration)
    // The configuration of the project can schedule a restart of the language services on the EDT.
    runInEdtAndWait {
      PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
    }
    disableAstLoadingFilter()
    TypeScriptServiceTestMixin.setUpTypeScriptService(myFixture) {
      it::class == TypeScriptGoLspService::class
    }
  }

  /**
   * Waits until the TypeScript 7 server opens [file]. The wait dispatches the events of the EDT.
   */
  private fun waitUntilOpenedByServer(file: VirtualFile) {
    runInEdtAndWait {
      waitUntilFileOpenedByLspServer(project, file)
    }
  }

  /**
   * Checks the highlighting of each file in [paths] against the markup of its test data file.
   * [astroContentMapper] installs the bundled `@astrojs/ts-content-mapper` into `node_modules`.
   */
  private fun checkHighlighting(
    vararg paths: String,
    astroContentMapper: Boolean = false,
  ) {
    doContentMapperTest(astroContentMapper = astroContentMapper, expectedHighlightingPaths = paths.toList()) {
      for (path in paths) {
        this@AstroTypeScriptGoContentMapperTest.checkFileHighlighting(path)
      }
    }
  }

  private fun doContentMapperTest(
    astroContentMapper: Boolean = false,
    dirName: String = defaultDirName,
    expectedHighlightingPaths: List<String> = emptyList(),
    test: CodeInsightTestFixture.() -> Unit,
  ) {
    val expectedData = ExpectedDataConfigurator(expectedHighlightingPaths)
    expectedDataConfigurator = expectedData
    currentDirName = dirName
    val configurators = buildList {
      add(AstroTypeScriptGoConfigurator())
      if (astroContentMapper) add(AstroContentMapperPackage())
      add(expectedData)
    }
    doConfiguredTest(dirName = dirName, configureFile = false, configurators = configurators) {
      test(this)
    }
  }

  /**
   * The server serves the diagnostics asynchronously, so a single highlighting pass can run before the pull response arrives.
   * A failed check waits for the next diagnostics from the server and runs again.
   */
  private fun checkFileHighlighting(path: String) {
    val file = myFixture.configureFromTempProjectFile(path).virtualFile
    waitUntilOpenedByServer(file)
    file.putUserData(VfsTestUtil.TEST_DATA_FILE_PATH, "$testDataPath/${currentDirName ?: defaultDirName}/$path")
    val data = expectedDataConfigurator!!.getData(path)
    var lastFailure: AssertionError? = null
    repeat(5) { attempt ->
      if (attempt > 0) {
        try {
          waitForDiagnosticsFromLspServer(project, file, timeout = 10)
        }
        catch (_: AssertionError) {
          // No new diagnostics arrived in time. The next attempt checks the current state anyway.
        }
      }
      try {
        (myFixture as CodeInsightTestFixtureImpl).collectAndCheckHighlighting(data)
        return
      }
      catch (failure: AssertionError) {
        lastFailure = failure
      }
    }
    throw lastFailure!!
  }

  private class ExpectedDataConfigurator(
    private val paths: List<String>,
  ) : PolySymbolsTestConfigurator {
    private val data = mutableMapOf<String, ExpectedHighlightingData>()

    fun getData(path: String): ExpectedHighlightingData =
      data.getValue(path)

    override fun configure(fixture: CodeInsightTestFixture) {
      runInEdtAndWait {
        val documentManager = FileDocumentManager.getInstance()
        for (path in paths) {
          val file = fixture.tempDirFixture.getFile(path) ?: error("No file $path in the test data")
          val document = documentManager.getDocument(file)!!
          data[path] = ExpectedHighlightingData(document, true, true, false).apply { init() }
        }
        documentManager.saveAllDocuments()
      }
    }
  }

  private class AstroContentMapperPackage : PolySymbolsTestConfigurator {
    override fun configure(fixture: CodeInsightTestFixture) {
      copyBundledAstroContentMapper(fixture)
    }
  }
}
