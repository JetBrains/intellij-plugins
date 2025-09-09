// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.javascript.testFramework.web.WebFrameworkTestCase
import com.intellij.lang.javascript.HybridTestMode
import com.intellij.lang.javascript.waitEmptyServiceQueueForService
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil.TypeScriptUseServiceState
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerServiceImpl
import com.intellij.lang.typescript.tsc.TypeScriptServiceTestMixin
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.registry.Registry
import com.intellij.testFramework.runInEdtAndWait
import org.angular2.lang.expr.service.Angular2TypeScriptService
import org.angular2.options.AngularServiceSettings
import org.angular2.options.configureAngularSettingsService
import kotlin.reflect.KClass

abstract class Angular2TestCase(
  override val testCasePath: String,
  private val useTsc: Boolean = true,
) : WebFrameworkTestCase(if (useTsc) HybridTestMode.CodeInsightFixture else HybridTestMode.BasePlatform) {

  private var expectedServerClass: KClass<out TypeScriptService> = Angular2TypeScriptService::class

  override val testDataRoot: String
    get() = Angular2TestUtil.getBaseTestDataPath()

  override val defaultDependencies: Map<String, String> =
    mapOf("@angular/core" to "*")

  override val defaultExtension: String
    get() = "ts"

  override fun setUp() {
    super.setUp()
    Registry.get("ast.loading.filter").setValue(false, testRootDisposable)
  }

  override fun beforeConfiguredTest(configuration: TestConfiguration) {
    if (useTsc) {
      configureAngularSettingsService(project, testRootDisposable, AngularServiceSettings.AUTO)
      val service = TypeScriptServiceTestMixin.setUpTypeScriptService(myFixture, TypeScriptUseServiceState.USE_FOR_EVERYTHING) {
        it::class == expectedServerClass
      }
      (service as TypeScriptServerServiceImpl).assertProcessStarted()
      runInEdtAndWait {
        waitEmptyServiceQueueForService(service)
      }

      if (configuration.configurators.any { it is Angular2TsConfigFile }) {
        TypeScriptServerServiceImpl.requireTSConfigsForTypeEvaluation(testRootDisposable, myFixture.tempDirFixture.getFile("tsconfig.json")!!)
      }
      runInEdtAndWait {
        FileDocumentManager.getInstance().saveAllDocuments()
      }
    }
  }

  override fun afterConfiguredTest(configuration: TestConfiguration) {
  }

  fun withTypeScriptServerService(clazz: KClass<out TypeScriptService>, runnable: () -> Unit) {
    expectedServerClass = clazz
    try {
      runnable()
    }
    finally {
      expectedServerClass = Angular2TypeScriptService::class
    }
  }

  protected fun checkHighlightingAndQuickFix(
    vararg modules: Angular2TestModule,
    quickFixName: String,
    dir: Boolean = false,
    extension: String = "ts",
    configureFileName: String = "$testName.$extension",
    inspections: Collection<Class<out LocalInspectionTool>> = emptyList(),
  ) = doConfiguredTest(*modules, dir = dir, extension = extension, configureFileName = configureFileName, checkResult = true) {
    enableInspections(inspections)
    this.checkHighlighting(true, false, true)
    this.launchAction(findSingleIntention(quickFixName))
  }
}