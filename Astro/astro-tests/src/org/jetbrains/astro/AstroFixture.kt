package org.jetbrains.astro

import com.intellij.javascript.testFramework.web.WebFrameworkFixture
import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.lang.javascript.library.typings.TypeScriptExternalDefinitionsRegistry
import com.intellij.lang.typescript.library.download.TypeScriptDefinitionFilesDirectory
import com.intellij.lang.typescript.tsc.TypeScriptServiceTestMixin
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.registry.RegistryManager
import com.intellij.platform.lsp.tests.waitUntilFileOpenedByLspServer
import com.intellij.polySymbols.testFramework.HybridFixtureContext
import com.intellij.polySymbols.testFramework.HybridTestMode
import com.intellij.polySymbols.testFramework.PolySymbolsTestConfigurator
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.junit5.fixture.TestFixture
import com.intellij.testFramework.runInEdtAndWait
import org.jetbrains.astro.service.AstroLspTypeScriptService
import org.jetbrains.astro.service.settings.AstroServiceMode
import org.jetbrains.astro.service.settings.getAstroServiceSettings
import org.jetbrains.annotations.TestOnly

/**
 * Creates an [AstroFixture] for the test data under [testCasePath].
 * With [useLsp], each test gets a new heavy project with the Astro language server. Otherwise the tests share the light project.
 * Declare the result as an instance property.
 */
@TestOnly
fun astroFixture(testCasePath: String, useLsp: Boolean = false): TestFixture<AstroFixture> =
  astroFixture(useLsp) { AstroFixture(it, testCasePath, useLsp) }

/**
 * Creates a sub-fixture of [AstroFixture] with [create]. [useLsp] selects the project, as in [astroFixture].
 */
@TestOnly
fun <T : AstroFixture> astroFixture(useLsp: Boolean, create: (HybridFixtureContext) -> T): TestFixture<T> =
  hybridFixture({ if (useLsp) HybridTestMode.CodeInsightFixture else HybridTestMode.BasePlatform }, create)

/**
 * A [WebFrameworkFixture] with the Astro test data root, the `astro` extension,
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