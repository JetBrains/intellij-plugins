package org.jetbrains.qodana.dart

import com.intellij.codeInspection.lang.InspectionExtensionsFactory
import com.intellij.openapi.application.PluginPathManager
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.HeavyTestHelper
import com.jetbrains.qodana.sarif.model.Location
import com.jetbrains.qodana.sarif.model.Result
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools.ExternalToolsProvider
import org.jetbrains.qodana.staticAnalysis.inspections.runner.globalOutput.GlobalOutputConsumer
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfileProvider
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import org.jetbrains.qodana.staticAnalysis.testFramework.reinstantiateInspectionRelatedServices
import java.nio.file.Files
import java.nio.file.Path

internal abstract class DartAnalyzeQodanaTestCase : QodanaRunnerTestCase() {
  protected open val analyzeResponses: Map<String, RecordedDartAnalyzeResponse> = emptyMap()
  private lateinit var analyzerExecutions: MutableList<Pair<String, String>>

  override val testData: Path = Path.of(PluginPathManager.getPluginHomePath("qodana"), "dart", "test-data")

  override fun setUp() {
    super.setUp()
    updateQodanaConfig { it.copy(profile = QodanaProfileConfig.named(DART_STARTER_PROFILE)) }
    ExternalToolsProvider.EXTERNAL_TOOLS_EP.point.registerExtension(
      DartAnalyzeInspectionDescriptorProvider(),
      testRootDisposable,
    )
    QodanaInspectionProfileProvider.EP_NAME.point.registerExtension(DartInspectionProfileProvider(), testRootDisposable)
    analyzerExecutions = mutableListOf()
    DartAnalyzeTestRunnerFactoryHolder.factory = DartAnalyzeTestRunnerFactory(
      Path.of(project.basePath!!),
      analyzeResponses,
      analyzerExecutions,
    ).create()
    Disposer.register(testRootDisposable) { DartAnalyzeTestRunnerFactoryHolder.factory = null }
    registerGlobalTool(DartAnalyzeTestGlobalInspection(), DartAnalyzeGlobalInspectionPresentation::class.java.name)
    GlobalOutputConsumer.EP_NAME.point.registerExtension(DartAnalyzeGlobalInspectionOutputConsumer(), testRootDisposable)
    InspectionExtensionsFactory.EP_NAME.point.registerExtension(
      DartAnalyzeGlobalInspectionContextFactory(),
      testRootDisposable,
    )
    reinstantiateInspectionRelatedServices(project, testRootDisposable)
  }

  protected fun singleSarifResult(): Result {
    val results = manager.sarifRun.results
    assertEquals("Expected exactly one SARIF result", 1, results.size)
    return results[0]
  }

  protected fun Result.singleLocation(): Location = locations.let {
    assertEquals("Expected exactly one location for SARIF result '$ruleId'", 1, it.size)
    it[0]
  }

  protected fun sarifResultPaths(): List<String> =
    manager.sarifRun.results.map { it.singleLocation().physicalLocation.artifactLocation.uri }.sorted()

  protected fun assertRecordedAnalyzedModules(vararg modules: String) {
    assertEquals(
      "Unexpected analyzed Dart modules",
      modules.toList().sorted(),
      analyzerExecutions.filter { it.first == "analyze" }.map { it.second }.sorted(),
    )
  }

  protected fun assertRecordedDartExecutions(vararg expected: Pair<String, String>) {
    assertEquals("Unexpected Dart executions", expected.toList(), analyzerExecutions)
  }
}

internal abstract class DartAnalyzeProjectQodanaTestCase : DartAnalyzeQodanaTestCase() {
  override val testDataBasePath: Path = Path.of(javaClass.simpleName)

  protected open val projectSourcePath: Path
    get() = testData.resolve(testDataBasePath).resolve("sources")

  override fun createTestStructure() {
    val projectRoot = Path.of(project.basePath!!)
    Files.createDirectories(projectRoot)
    HeavyTestHelper.createTestProjectStructure(myModule, projectSourcePath.toString(), projectRoot, true)
    PsiDocumentManager.getInstance(myProject).commitAllDocuments()
  }
}
