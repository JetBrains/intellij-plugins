package org.jetbrains.astro

import com.intellij.javascript.testFramework.web.WebFrameworkFixture
import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.lang.javascript.library.typings.TypeScriptExternalDefinitionsRegistry
import com.intellij.lang.typescript.library.download.TypeScriptDefinitionFilesDirectory
import com.intellij.lang.typescript.tsc.TypeScriptServiceTestMixin
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.registry.RegistryManager
import com.intellij.platform.lsp.tests.waitUntilFileOpenedByLspServer
import com.intellij.polySymbols.testFramework.HybridFixtureContext
import com.intellij.polySymbols.testFramework.HybridTestMode
import com.intellij.polySymbols.testFramework.PolySymbolsTestConfigurator
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.runInEdtAndWait
import org.jetbrains.astro.service.AstroLspTypeScriptService
import org.jetbrains.astro.service.settings.AstroServiceMode
import org.jetbrains.astro.service.settings.getAstroServiceSettings

/**
 * The base of the Astro code insight tests. The fixture [astro] is an [AstroFixture].
 * Each test gets a new project. With [useLsp], the project is a heavy project with the Astro language server.
 */
abstract class AstroCodeInsightTestCase protected constructor(
  testCasePath: String,
  useLsp: Boolean,
  createFixture: (context: HybridFixtureContext, testCasePath: String, useLsp: Boolean) -> AstroFixture,
) {

  constructor(testCasePath: String, useLsp: Boolean = false) : this(testCasePath, useLsp, ::AstroFixture)

  protected val astro: AstroFixture by hybridFixture({ if (useLsp) HybridTestMode.CodeInsightFixture else HybridTestMode.BasePlatform }) {
    createFixture(it, testCasePath, useLsp)
  }

  protected val myFixture: CodeInsightTestFixture get() = astro.fixture

  protected val project: Project get() = astro.project
}

/**
 * The state of [AstroCodeInsightTestCase]: a [WebFrameworkFixture] with the Astro test data root, the `astro` extension,
 * and the `astro` dependency.
 * With [useLsp], the fixture enables the Astro language server, adds a `tsconfig.json` file and the [AstroTestModule.ASTRO_5_14_4] module,
 * and waits until the server opens the configured file.
 * Call a member from a background thread.
 */
open class AstroFixture(
  context: HybridFixtureContext,
  testCasePath: String,
  val useLsp: Boolean,
) : WebFrameworkFixture(
  context,
  testDataRoot = getAstroTestDataPath(),
  testCasePath = testCasePath,
  defaultExtension = "astro",
  defaultDependencies = mapOf("astro" to "5.14.4"),
) {

  override fun adjustConfigurators(
    configurators: List<PolySymbolsTestConfigurator>,
  ): List<PolySymbolsTestConfigurator> =
    buildList {
      addAll(super.adjustConfigurators(configurators))

      if (useLsp) {
        add(AstroLspConfigurator())
        add(AstroTsConfigConfigurator(defaultTsconfigJsonContent))
      }
    }

  private val defaultTsconfigJsonContent: String = ("""
    {
      "extends": "astro/tsconfigs/strict",
      "include": [".astro/types.d.ts", "**/*"],
      "exclude": ["dist"],
      "compilerOptions": {
        "strictNullChecks": true,
        "allowJs": true,
      }
    }
    """.trimIndent())

  override fun beforeConfiguredTest(configuration: TestConfiguration) {
    super.beforeConfiguredTest(configuration)
    if (useLsp) {
      // The wait dispatches the events of the EDT.
      runInEdtAndWait {
        waitUntilFileOpenedByLspServer(project, fixture.file.virtualFile)
      }
    }
  }

  override fun adjustModules(modules: Array<out WebFrameworkTestModule>): Array<out WebFrameworkTestModule> {
    val base = super.adjustModules(modules)
    if (!useLsp) return base
    return if (base.any { it == AstroTestModule.ASTRO_5_14_4 }) base else arrayOf(*base, AstroTestModule.ASTRO_5_14_4)
  }
}

private class AstroLspConfigurator : PolySymbolsTestConfigurator {
  override fun configure(fixture: CodeInsightTestFixture) {
    val serviceSettings = getAstroServiceSettings(fixture.project)

    val old = serviceSettings.serviceMode

    val contextDisposable = fixture.testRootDisposable
    Disposer.register(contextDisposable) {
      serviceSettings.serviceMode = old
    }

    serviceSettings.serviceMode = AstroServiceMode.ENABLED
    // The project outlives the test root disposable. The fixture removes the module before it closes the project,
    // and the language server integration checks the bundled server again on this change.
    RegistryManager.getInstance().get("astro.language.server.bundled.enabled").setValue(true, fixture.project)
    TypeScriptExternalDefinitionsRegistry.testTypingsRootPath = TypeScriptDefinitionFilesDirectory.getGlobalAutoDownloadTypesDirectoryPath()
    TypeScriptServiceTestMixin.setUpTypeScriptService(fixture) {
      it::class == AstroLspTypeScriptService::class
    }
  }
}

private class AstroTsConfigConfigurator(private val tsconfigJsonContent: String) : PolySymbolsTestConfigurator {
  override fun configure(fixture: CodeInsightTestFixture) {
    if (fixture.tempDirFixture.getFile("tsconfig.json") == null) {
      fixture.addFileToProject("tsconfig.json", tsconfigJsonContent)
    }
  }
}