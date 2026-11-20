package org.jetbrains.astro

import com.intellij.javascript.testFramework.web.WebFrameworkTestCase
import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.lang.javascript.library.typings.TypeScriptExternalDefinitionsRegistry
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings.TypeScriptCompilerVersionType
import com.intellij.lang.typescript.library.download.TypeScriptDefinitionFilesDirectory
import com.intellij.lang.typescript.lsp.TypeScriptGoLspService
import com.intellij.lang.typescript.tsc.TypeScriptServiceTestMixin
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.util.registry.RegistryManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.platform.lsp.tests.waitUntilFileOpenedByLspServer
import com.intellij.polySymbols.testFramework.HybridTestMode
import com.intellij.polySymbols.testFramework.PolySymbolsTestConfigurator
import com.intellij.polySymbols.testFramework.disableAstLoadingFilter
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.runInEdtAndWait
import org.jetbrains.astro.service.AstroLspTypeScriptService
import org.jetbrains.astro.service.astroContentMapperPackageName
import org.jetbrains.astro.service.getBundledAstroContentMapperDir
import org.jetbrains.astro.service.settings.AstroServiceMode
import org.jetbrains.astro.service.settings.getAstroServiceSettings
import java.io.File

enum class AstroTestMode {
  /**
   * The test configures no language service. The tests share the light project.
   */
  NO_SERVICE,

  /**
   * The Astro language server serves the project.
   */
  ASTRO_LSP,

  /**
   * TypeScript 7 serves the project. It maps the `.astro` files with the bundled `@astrojs/ts-content-mapper`.
   */
  TS_GO_PROXY,
}

/**
 * Copies the bundled `@astrojs/ts-content-mapper` package into `node_modules` of the test project.
 */
fun copyBundledAstroContentMapper(fixture: CodeInsightTestFixture) {
  fixture.tempDirFixture.copyAll(getBundledAstroContentMapperDir()!!.toString(), "node_modules/$astroContentMapperPackageName")
}

/**
 * A [WebFrameworkTestCase] with the Astro test data root, the `astro` extension, and the `astro` dependency.
 * Without [AstroTestMode.NO_SERVICE], each test gets a new heavy project. [hybridTestMode] can override the project kind.
 * The test case adds a `tsconfig.json` file and the [AstroTestModule.ASTRO_5_14_4] module,
 * starts the language service of [testMode], and waits until the server opens the configured file.
 * With [AstroTestMode.TS_GO_PROXY], the `tsconfig.json` file maps the `.astro` files with the bundled content mapper.
 * In this mode, the test case reads the `.tsgo` variant of an expected data file, if the variant exists.
 * The variant has the `.tsgo` infix before the extension, for example `htmlElements.items.tsgo.txt`.
 */
abstract class AstroCodeInsightTestCase(
  override val testCasePath: String,
  val testMode: AstroTestMode = AstroTestMode.NO_SERVICE,
  hybridTestMode: HybridTestMode = if (testMode != AstroTestMode.NO_SERVICE) HybridTestMode.CodeInsightFixture else HybridTestMode.BasePlatform,
) : WebFrameworkTestCase(hybridTestMode) {

  override fun adjustConfigurators(
    configurators: List<PolySymbolsTestConfigurator>,
  ): List<PolySymbolsTestConfigurator> =
    buildList {
      addAll(super.adjustConfigurators(configurators))

      when (testMode) {
        AstroTestMode.NO_SERVICE -> {}

        AstroTestMode.ASTRO_LSP -> {
          add(AstroLspConfigurator())
          add(AstroTsConfigConfigurator(defaultTsconfigJsonContent, contentMapper = false))
        }

        AstroTestMode.TS_GO_PROXY -> {
          add(AstroTypeScriptGoConfigurator())
          add(AstroTsConfigConfigurator(defaultTsconfigJsonContent, contentMapper = true))
          add(AstroTypeScriptGoServiceConfigurator())
        }
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
    when (testMode) {
      AstroTestMode.NO_SERVICE -> return

      AstroTestMode.ASTRO_LSP -> {}

      AstroTestMode.TS_GO_PROXY -> disableAstLoadingFilter()
    }
    waitUntilFileOpenedByLspServer(project, myFixture.file.virtualFile)
  }

  override fun adjustModules(modules: Array<out WebFrameworkTestModule>): Array<out WebFrameworkTestModule> {
    val base = super.adjustModules(modules)
    if (testMode == AstroTestMode.NO_SERVICE) return base
    return if (base.any { it == AstroTestModule.ASTRO_5_14_4 }) base else arrayOf(*base, AstroTestModule.ASTRO_5_14_4)
  }

  override fun getDefaultConfigureFileName(extension: String): String =
    tsGoTestDataFile(super.getDefaultConfigureFileName(extension))

  override fun getGoldFileName(forcedGoldFileName: String?, testFileExt: String): String =
    tsGoTestDataFile(super.getGoldFileName(forcedGoldFileName, testFileExt))

  override fun getCodeCompletionExpectedItemsFileNameInfix(prefix: String, suffix: String): String =
    tsGoInfix(prefix, suffix)

  val expectedDocumentationFileSuffix: String
    get() = ".expected" + tsGoInfix("$testName.expected", ".html")

  /**
   * Returns the `.tsgo` variant of the test data file at [path], if the variant exists. Otherwise, the function returns [path].
   * [path] is relative to [testDataPath].
   */
  fun tsGoTestDataFile(path: String): String {
    val extensionIndex = path.lastIndexOf('.').takeIf { it > path.lastIndexOf('/') } ?: path.length
    val prefix = path.substring(0, extensionIndex)
    val suffix = path.substring(extensionIndex)
    return prefix + tsGoInfix(prefix, suffix) + suffix
  }

  private fun tsGoInfix(prefix: String, suffix: String): String =
    if (testMode == AstroTestMode.TS_GO_PROXY && File("$testDataPath/$prefix$TS_GO_INFIX$suffix").exists()) TS_GO_INFIX else ""
}

private const val TS_GO_INFIX = ".tsgo"

/**
 * Starts the TypeScript 7 service before the configured file opens in the editor, so that the server opens the file.
 */
private class AstroTypeScriptGoServiceConfigurator : PolySymbolsTestConfigurator {
  override fun configure(fixture: CodeInsightTestFixture) {
    // The configuration of the project can schedule a restart of the language services on the EDT.
    runInEdtAndWait {
      PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
    }
    TypeScriptServiceTestMixin.setUpTypeScriptService(fixture) {
      it::class == TypeScriptGoLspService::class
    }
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
    RegistryManager.getInstance().get("astro.language.server.bundled.enabled").setValue(true, contextDisposable)
    TypeScriptExternalDefinitionsRegistry.testTypingsRootPath = TypeScriptDefinitionFilesDirectory.getGlobalAutoDownloadTypesDirectoryPath()
    TypeScriptServiceTestMixin.setUpTypeScriptService(fixture) {
      it::class == AstroLspTypeScriptService::class
    }
  }
}

/**
 * Turns on TypeScript 7 and the Astro services before the configured file opens in the editor.
 * The Astro services stay inactive, because TypeScript 7 serves the project.
 */
class AstroTypeScriptGoConfigurator : PolySymbolsTestConfigurator {
  override fun configure(fixture: CodeInsightTestFixture) {
    Registry.get("typescript.ts-go.enabled").setValue(true, fixture.testRootDisposable)
    TypeScriptExternalDefinitionsRegistry.testTypingsRootPath = TypeScriptDefinitionFilesDirectory.getGlobalAutoDownloadTypesDirectoryPath()

    val tsCompilerSettings = TypeScriptCompilerSettings.getSettings(fixture.project)
    val oldVersionType = tsCompilerSettings.versionType
    tsCompilerSettings.versionType = TypeScriptCompilerVersionType.TS_GO_PROXY_RECOMMENDED_VERSION

    val serviceSettings = getAstroServiceSettings(fixture.project)
    val oldServiceMode = serviceSettings.serviceMode
    serviceSettings.serviceMode = AstroServiceMode.ENABLED

    // The settings return when the project closes, so the open editors do not start the Astro services.
    Disposer.register(fixture.project) {
      tsCompilerSettings.versionType = oldVersionType
      serviceSettings.serviceMode = oldServiceMode
    }
  }
}

/**
 * Adds a `tsconfig.json` file with [tsconfigJsonContent], if the test data has no such file.
 * With [contentMapper], the configurator copies the bundled `@astrojs/ts-content-mapper` into `node_modules`,
 * and the `tsconfig.json` file maps the `.astro` files with it.
 */
private class AstroTsConfigConfigurator(
  private val tsconfigJsonContent: String,
  private val contentMapper: Boolean,
) : PolySymbolsTestConfigurator {
  override fun configure(fixture: CodeInsightTestFixture) {
    if (contentMapper) {
      copyBundledAstroContentMapper(fixture)
    }
    val file = fixture.tempDirFixture.getFile("tsconfig.json")
    if (file == null) {
      fixture.addFileToProject("tsconfig.json", if (contentMapper) withContentMapper(tsconfigJsonContent) else tsconfigJsonContent)
    }
    else if (contentMapper) {
      val text = VfsUtil.loadText(file)
      if (!text.contains("\"contentMappers\"")) {
        runInEdtAndWait {
          WriteAction.run<Throwable> { VfsUtil.saveText(file, withContentMapper(text)) }
        }
      }
    }
  }

  /**
   * Adds the `contentMappers` property at the start of the root object. A tsconfig file permits a trailing comma.
   */
  private fun withContentMapper(tsconfigJsonContent: String): String {
    val start = tsconfigJsonContent.indexOf('{')
    check(start >= 0) { "No root object in tsconfig.json" }
    val contentMappers = """"contentMappers": [{ "package": "$astroContentMapperPackageName", "extensions": [".astro"] }],"""
    return tsconfigJsonContent.substring(0, start + 1) + "\n  " + contentMappers + tsconfigJsonContent.substring(start + 1)
  }
}
