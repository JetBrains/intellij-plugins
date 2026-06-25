// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.javascript.testFramework.web.WebFrameworkTestCase
import com.intellij.lang.javascript.waitEmptyServiceQueueForService
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerServiceImpl
import com.intellij.lang.typescript.lsp.TypeScriptGoLspClientDescriptor
import com.intellij.lang.typescript.lsp.TypeScriptGoLspIntegrationProvider
import com.intellij.lang.typescript.lsp.TypeScriptGoLspService
import com.intellij.lang.typescript.tsc.TypeScriptGoTypeEvaluatorMode
import com.intellij.lang.typescript.tsc.TypeScriptServiceTestMixin
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.platform.lsp.api.LspClientDescriptor
import com.intellij.platform.lsp.api.LspClientManager
import com.intellij.platform.lsp.api.LspIntegrationProvider
import com.intellij.platform.lsp.impl.LspClientImpl
import com.intellij.platform.lsp.impl.LspClientManagerImpl
import com.intellij.polySymbols.testFramework.HybridTestMode
import com.intellij.testFramework.PlatformTestUtil.dispatchAllEventsInIdeEventQueue
import com.intellij.testFramework.replaceService
import com.intellij.testFramework.runInEdtAndWait
import com.intellij.util.application
import org.angular2.codeInsight.Angular2HighlightingTest
import org.angular2.codeInsight.refactoring.Angular2CliComponentGeneratorMockImpl
import org.angular2.lang.expr.service.Angular2TypeScriptService
import org.angular2.options.AngularServiceSettings
import org.angular2.options.configureAngularSettingsService
import org.angular2.refactoring.extractComponent.Angular2CliComponentGenerator
import org.junit.Assume
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class TestNoService

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class TestTsNode

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class TestTsGoFork

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class TestTsGoProxy

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class SkipNoService

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class SkipTsNode

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class SkipTsGoFork

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class SkipTsGoProxy

@RunWith(com.intellij.testFramework.Parameterized::class)
abstract class Angular2TestCase(
  override val testCasePath: String,
) : WebFrameworkTestCase() {

  enum class TypeScriptServiceKind(val annotationClass: KClass<out Annotation>, val skipTestAnnotationClass: KClass<out Annotation>) {
    None(TestNoService::class, SkipNoService::class),
    TsNode(TestTsNode::class, SkipTsNode::class),
    TsGoFork(TestTsGoFork::class, SkipTsGoFork::class),
    TsGoProxy(TestTsGoProxy::class, SkipTsGoProxy::class),
  }

  @Parameterized.Parameter
  @JvmField
  var serviceKind: TypeScriptServiceKind = TypeScriptServiceKind.None

  private val expectedServerClass: KClass<out TypeScriptService> by lazy(LazyThreadSafetyMode.PUBLICATION) {
    when (serviceKind) {
      TypeScriptServiceKind.TsNode -> Angular2TypeScriptService::class
      TypeScriptServiceKind.TsGoFork -> TypeScriptGoLspService::class
      TypeScriptServiceKind.TsGoProxy -> TypeScriptGoLspService::class
      TypeScriptServiceKind.None -> Angular2TypeScriptService::class
    }
  }

  override fun initializeTestMode(): HybridTestMode =
    if (serviceKind == TypeScriptServiceKind.None) {
      HybridTestMode.BasePlatform
    }
    else {
      HybridTestMode.CodeInsightFixture
    }

  override val testDataRoot: String
    get() = Angular2TestUtil.getBaseTestDataPath()

  override val defaultDependencies: Map<String, String> =
    mapOf("@angular/core" to "*")

  override val defaultExtension: String
    get() = "ts"

  override val defaultDirName: String get() {
    if (serviceKind == TypeScriptServiceKind.TsGoProxy) {
      val tsGoDirName = "$testName.tsgo"
      if (File("$testDataPath/$tsGoDirName").exists())
        return tsGoDirName
    }
    return testName
  }

  override fun getDefaultConfigureFileName(extension: String): String {
    if (serviceKind == TypeScriptServiceKind.TsGoProxy) {
      val tsgoConfigureFileName = "$testName.tsgo.$extension"
      if (File("$testDataPath/$tsgoConfigureFileName").exists())
        return tsgoConfigureFileName
    }
    return super.getDefaultConfigureFileName(extension)
  }

  override fun getGoldFileName(forcedGoldFileName: String?, testFileExt: String): String {
    val goldFileName = super.getGoldFileName(forcedGoldFileName, testFileExt)
    if (serviceKind == TypeScriptServiceKind.TsGoProxy) {
      val lastIndex = goldFileName.lastIndexOf('.')
      val tsGoGoldFileName = "${goldFileName.substring(0, lastIndex)}.tsgo${goldFileName.substring(lastIndex)}"
      if (File("$testDataPath/$tsGoGoldFileName").exists())
        return tsGoGoldFileName
    }
    return goldFileName
  }

  override fun setUp() {
    Assume.assumeTrue("Skipping test because of @${serviceKind.skipTestAnnotationClass.simpleName} annotation",
                      javaClass.getMethod(name).annotations.none { it.annotationClass == serviceKind.skipTestAnnotationClass })

    Assume.assumeTrue("Skipping flaky TS GO Proxy tests",
                      serviceKind != TypeScriptServiceKind.TsGoProxy || this is Angular2HighlightingTest)
    super.setUp()

    myFixture.project.replaceService(
      Angular2CliComponentGenerator::class.java,
      Angular2CliComponentGeneratorMockImpl(project),
      testRootDisposable
    )
    Registry.get("ast.loading.filter").setValue(false, testRootDisposable)
  }

  override fun beforeConfiguredTest(configuration: TestConfiguration) {
    when (serviceKind) {
      TypeScriptServiceKind.None -> return
      TypeScriptServiceKind.TsNode -> {}
      TypeScriptServiceKind.TsGoFork,
      TypeScriptServiceKind.TsGoProxy,
        -> Registry.get("typescript.ts-go.enabled").setValue(true, testRootDisposable)
    }
    configureAngularSettingsService(project, testRootDisposable, AngularServiceSettings.AUTO)
    val service = TypeScriptServiceTestMixin.setUpTypeScriptService(
      myFixture,
      tsGoTypeEvaluatorMode = if (serviceKind == TypeScriptServiceKind.TsGoProxy)
        TypeScriptGoTypeEvaluatorMode.PROXY
      else
        TypeScriptGoTypeEvaluatorMode.TS_GO_FORK
    ) {
      it::class == expectedServerClass
    }
    thisLogger().info("Using $service for the test")
    when (serviceKind) {
      TypeScriptServiceKind.TsNode -> {
        (service as TypeScriptServerServiceImpl).assertProcessStarted()
        runInEdtAndWait {
          waitEmptyServiceQueueForService(service)
        }
      }
      TypeScriptServiceKind.TsGoFork,
      TypeScriptServiceKind.TsGoProxy,
        -> {
        triggerLspServerInit(project, TypeScriptGoLspIntegrationProvider::class.java,
                             TypeScriptGoLspClientDescriptor(project))
      }
      TypeScriptServiceKind.None -> {}
    }

    if (configuration.configurators.any { it is Angular2TsConfigFile }) {
      TypeScriptServerServiceImpl.requireTSConfigsForTypeEvaluation(
        testRootDisposable,
        myFixture.tempDirFixture.getFile("tsconfig.json")!!)
    }
    runInEdtAndWait {
      FileDocumentManager.getInstance().saveAllDocuments()
    }
  }

  override fun afterConfiguredTest(configuration: TestConfiguration) {
  }

  protected fun checkHighlightingAndQuickFix(
    vararg modules: Angular2TestModule,
    quickFixName: String,
    dir: Boolean = false,
    extension: String = "ts",
    configureFileName: String = getDefaultConfigureFileName(extension),
    inspections: Collection<Class<out LocalInspectionTool>> = emptyList(),
  ) = doConfiguredTest(*modules, dir = dir, extension = extension, configureFileName = configureFileName, checkResult = true) {
    enableInspections(inspections)
    this.checkHighlighting(true, false, true)
    this.launchAction(findSingleIntention(quickFixName))
  }

  private fun triggerLspServerInit(
    project: Project,
    providerClass: Class<out LspIntegrationProvider>,
    descriptor: LspClientDescriptor,
  ) {
    val getServer = {
      LspClientManager.getInstance(project)
        .getClients(providerClass)
        .firstOrNull()
        .let { it as? LspClientImpl }
    }

    val state = getServer()?.state
    if (state == null) {
      LspClientManagerImpl.getInstanceImpl(project)
        .ensureClientStarted(providerClass, descriptor)
    }
    else {
      return
    }
    val isEDT = ApplicationManager.getApplication().isDispatchThread
    val start = System.currentTimeMillis()
    while (System.currentTimeMillis() - start < 4000) {
      val state = getServer()?.state
      if (state != null)
        return
      if (isEDT) {
        dispatchAllEventsInIdeEventQueue()
      }
      Thread.sleep(10)
    }

    if (application.isUnitTestMode)
      throw IllegalStateException("Server didn't initialize in 4000 ms")
  }

  companion object {
    @com.intellij.testFramework.Parameterized.Parameters(name = "ServiceKind={0}")
    @JvmStatic
    fun data(clazz: Class<*>): Collection<Any> {
      return TypeScriptServiceKind.entries
        .filter { clazz.getAnnotation(it.annotationClass.java) != null }
        .map { arrayOf<Any>(it) }
    }

    @Parameterized.Parameters
    @JvmStatic
    fun data(): Collection<Any> = emptyList()
  }
}