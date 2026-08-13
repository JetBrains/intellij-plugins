package org.jetbrains.qodana.dart

import com.intellij.codeInspection.lang.InspectionExtensionsFactory
import com.intellij.openapi.application.PluginPathManager
import com.intellij.testFramework.TestDataPath
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileYamlConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools.ExternalToolsProvider
import org.jetbrains.qodana.staticAnalysis.inspections.runner.globalOutput.GlobalOutputConsumer
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import org.jetbrains.qodana.staticAnalysis.testFramework.reinstantiateInspectionRelatedServices
import org.junit.Test
import java.nio.file.Path

@TestDataPath($$"$CONTENT_ROOT/../test-data/DartNestedModuleAnalysisOptionsTest")
class DartNestedModuleAnalysisOptionsTest : QodanaRunnerTestCase() {
  override val testData: Path = Path.of(PluginPathManager.getPluginHomePath("qodana"), "dart", "test-data")
  override val testDataBasePath: Path = Path.of(javaClass.simpleName)

  override fun setUp() {
    super.setUp()
    ExternalToolsProvider.EXTERNAL_TOOLS_EP.point.registerExtension(
      DartAnalyzeInspectionDescriptorProvider(),
      testRootDisposable,
    )
    DartAnalyzeTestRunnerFactoryHolder.factory = DartAnalyzeTestRunnerFactory(
      Path.of(project.basePath!!),
      mapOf(
        "test-module" to readDartAnalyzeTestData("nested-root.json"),
        "test-module/core-lints" to readDartAnalyzeTestData("nested-core-lints.json"),
        "test-module/no-lints" to readDartAnalyzeTestData("no-diagnostics.json"),
        "test-module/submodule" to readDartAnalyzeTestData("nested-submodule.json"),
      ),
    ).create()
    registerGlobalTool(DartAnalyzeTestGlobalInspection(), DartAnalyzeGlobalInspectionPresentation::class.java.name)
    GlobalOutputConsumer.EP_NAME.point.registerExtension(DartAnalyzeGlobalInspectionOutputConsumer(), testRootDisposable)
    InspectionExtensionsFactory.EP_NAME.point.registerExtension(
      DartAnalyzeGlobalInspectionContextFactory(),
      testRootDisposable,
    )
    reinstantiateInspectionRelatedServices(project, testRootDisposable)
  }

  @Test
  fun `uses analysis options from a nested Dart module`() {
    runDartAnalysis()
    assertSarifResultCodes(
      listOf(
        "test-module/root.dart:DartAnalyze.unnecessary_late",
        "test-module/root.dart:DartAnalyze.unnecessary_this",
        "test-module/lib/included.dart:DartAnalyze.unnecessary_late",
        "test-module/submodule/submodule.dart:DartAnalyze.prefer_final_fields",
        "test-module/submodule/submodule.dart:DartAnalyze.unnecessary_this",
      ),
    )
  }

  @Test
  fun `reports diagnostics from an included lib file`() {
    runDartAnalysis()
    val diagnosticCodes = manager.sarifRun.results
      .filter { diagnostic ->
        diagnostic.locations.single().physicalLocation.artifactLocation.uri.equals("test-module/lib/included.dart")
      }
      .map { it.ruleId }
    assertEquals(listOf("DartAnalyze.unnecessary_late"), diagnosticCodes)
  }

  @Test
  fun `reports only enabled Dart diagnostics`() {
    updateQodanaConfig { it.copy(profile = QodanaProfileConfig.named("qodana.single:DartAnalyze.unnecessary_this")) }
    runAnalysis()

    assertSarifResultCodes(
      listOf(
        "test-module/root.dart:DartAnalyze.unnecessary_this",
        "test-module/submodule/submodule.dart:DartAnalyze.unnecessary_this",
      ),
    )
  }

  @Test
  fun `reports core lints only when they are configured by the Dart module`() {
    updateQodanaConfig { it.copy(profile = dartAnalyzeCoreProfile()) }
    runAnalysis()

    val actual = manager.sarifRun.results.map { result ->
      val location = result.locations.single().physicalLocation.artifactLocation.uri
      "$location:${result.ruleId}"
    }.sorted()
    assertEquals(listOf("test-module/core-lints/lib/await_only_futures.dart:DartAnalyze.await_only_futures"), actual)
  }

  private fun runDartAnalysis() {
    updateQodanaConfig { it.copy(profile = dartAnalyzeProfile()) }
    runAnalysis()
  }

  private fun assertSarifResultCodes(expected: List<String>) {
    val actual = manager.sarifRun.results.map { result ->
      val location = result.locations.single().physicalLocation.artifactLocation.uri
      "$location:${result.ruleId}"
    }.sorted()
    assertEquals(expected.sorted(), actual)
  }

  private fun dartAnalyzeCoreProfile(): QodanaProfileConfig =
    QodanaProfileConfig.named("qodana.single:DartAnalyze.await_only_futures")

  private fun dartAnalyzeProfile(): QodanaProfileConfig = QodanaProfileConfig.named("qodana.single:DartAnalyzeUncategorized").copy(
    inspections = listOf(
      QodanaProfileYamlConfig.InspectionConfig("DartAnalyze.prefer_final_fields", enabled = true),
      QodanaProfileYamlConfig.InspectionConfig("DartAnalyze.unnecessary_late", enabled = true),
      QodanaProfileYamlConfig.InspectionConfig("DartAnalyze.unnecessary_this", enabled = true),
    ),
  )
}
