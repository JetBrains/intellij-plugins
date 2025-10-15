package org.jetbrains.astro

import com.intellij.javascript.testFramework.web.WebFrameworkTestConfigurator
import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.lang.javascript.HybridTestMode
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil.TypeScriptUseServiceState
import com.intellij.lang.typescript.tsc.TypeScriptServiceTestMixin
import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.registry.RegistryManager
import com.intellij.platform.lsp.tests.waitUntilFileOpenedByLspServer
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.jetbrains.astro.service.AstroLspTypeScriptService
import org.jetbrains.astro.service.settings.AstroServiceMode
import org.jetbrains.astro.service.settings.getAstroServiceSettings

abstract class AstroLspTestCase(testCasePath: String) : AstroCodeInsightTestCase(testCasePath, HybridTestMode.CodeInsightFixture) {

  override fun beforeConfiguredTest(configuration: TestConfiguration) {
    super.beforeConfiguredTest(configuration)
    waitUntilFileOpenedByLspServer(project, myFixture.file.virtualFile)
  }

  override fun doConfiguredTest(
    vararg modules: WebFrameworkTestModule,
    fileContents: String?,
    dir: Boolean,
    dirName: String,
    extension: String,
    configureFile: Boolean,
    configureFileName: String,
    configurators: List<WebFrameworkTestConfigurator>,
    additionalFiles: List<String>,
    checkResult: Boolean,
    editorConfigEnabled: Boolean,
    configureCodeStyleSettings: (CodeStyleSettings.() -> Unit)?,
    test: CodeInsightTestFixture.() -> Unit,
  ) {
    val combinedConfigurators = configurators + AstroLspConfigurator()
    super.doConfiguredTest(
      *modules,
      fileContents = fileContents,
      dir = dir,
      dirName = dirName,
      extension = extension,
      configureFile = configureFile,
      configureFileName = configureFileName,
      configurators = combinedConfigurators,
      additionalFiles = additionalFiles,
      checkResult = checkResult,
      editorConfigEnabled = editorConfigEnabled,
      configureCodeStyleSettings = configureCodeStyleSettings,
      test = test
    )
  }
}

class AstroLspConfigurator : WebFrameworkTestConfigurator {
  override fun configure(fixture: CodeInsightTestFixture, disposable: Disposable?) {
    val serviceSettings = getAstroServiceSettings(fixture.project)

    val old = serviceSettings.serviceMode

    val contextDisposable = disposable ?: fixture.testRootDisposable
    Disposer.register(contextDisposable) {
      serviceSettings.serviceMode = old
    }

    serviceSettings.serviceMode = AstroServiceMode.ENABLED
    RegistryManager.getInstance().get("astro.language.server.bundled.enabled").setValue(true, contextDisposable)
    TypeScriptServiceTestMixin.setUpTypeScriptService(fixture, TypeScriptUseServiceState.USE_FOR_EVERYTHING) {
      it::class == AstroLspTypeScriptService::class
    }
  }
}