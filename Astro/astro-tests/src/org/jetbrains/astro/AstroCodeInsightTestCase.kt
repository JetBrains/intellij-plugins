package org.jetbrains.astro

import com.intellij.javascript.testFramework.web.WebFrameworkTestCase
import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.lang.javascript.library.typings.TypeScriptExternalDefinitionsRegistry
import com.intellij.lang.typescript.library.download.TypeScriptDefinitionFilesDirectory
import com.intellij.lang.typescript.tsc.TypeScriptServiceTestMixin
import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.registry.RegistryManager
import com.intellij.platform.lsp.tests.waitUntilFileOpenedByLspServer
import com.intellij.polySymbols.testFramework.HybridTestMode
import com.intellij.polySymbols.testFramework.PolySymbolsTestConfigurator
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.jetbrains.astro.service.AstroLspTypeScriptService
import org.jetbrains.astro.service.settings.AstroServiceMode
import org.jetbrains.astro.service.settings.getAstroServiceSettings

abstract class AstroCodeInsightTestCase(
  override val testCasePath: String,
  val useLsp: Boolean = false,
) : WebFrameworkTestCase(if (useLsp) HybridTestMode.CodeInsightFixture else HybridTestMode.BasePlatform) {

  override val defaultConfigurators: List<PolySymbolsTestConfigurator>
    get() = buildList {
      addAll(super.defaultConfigurators)

      if (useLsp) {
        add(AstroLspConfigurator())
        add(AstroTsConfigConfigurator(defaultTsconfigJsonContent))
      }
    }

  protected open val defaultTsconfigJsonContent: String = ("""
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

  override val testDataRoot: String
    get() = getAstroTestDataPath()

  override val defaultDependencies: Map<String, String> = mapOf("astro" to "5.14.4")

  override val defaultExtension: String
    get() = "astro"

  override fun beforeConfiguredTest(configuration: TestConfiguration) {
    super.beforeConfiguredTest(configuration)
    if (useLsp) {
      waitUntilFileOpenedByLspServer(project, myFixture.file.virtualFile)
    }
  }

  override fun adjustModules(modules: Array<out WebFrameworkTestModule>): Array<out WebFrameworkTestModule> {
    val base = super.adjustModules(modules)
    if (!useLsp) return base
    return if (base.any { it == AstroTestModule.ASTRO_5_14_4 }) base else arrayOf(*base, AstroTestModule.ASTRO_5_14_4)
  }
}

private class AstroLspConfigurator : PolySymbolsTestConfigurator {
  override fun configure(fixture: CodeInsightTestFixture, disposable: Disposable?) {
    val serviceSettings = getAstroServiceSettings(fixture.project)

    val old = serviceSettings.serviceMode

    val contextDisposable = disposable ?: fixture.testRootDisposable
    Disposer.register(contextDisposable) {
      serviceSettings.serviceMode = old
    }

    serviceSettings.serviceMode = AstroServiceMode.ENABLED
    RegistryManager.getInstance().get("astro.language.server.bundled.enabled").setValue(true, contextDisposable)
    TypeScriptExternalDefinitionsRegistry.testTypingsRootPath = TypeScriptDefinitionFilesDirectory.getGlobalAutoDownloadTypesDirectoryPath()
    TypeScriptServiceTestMixin.setUpTypeScriptService(fixture) {
      it::class == AstroLspTypeScriptService::class
    }
  }
}

private class AstroTsConfigConfigurator(private val tsconfigJsonContent: String) : PolySymbolsTestConfigurator {
  override fun configure(fixture: CodeInsightTestFixture, disposable: Disposable?) {
    if (fixture.tempDirFixture.getFile("tsconfig.json") == null) {
      fixture.addFileToProject("tsconfig.json", tsconfigJsonContent)
    }
  }
}