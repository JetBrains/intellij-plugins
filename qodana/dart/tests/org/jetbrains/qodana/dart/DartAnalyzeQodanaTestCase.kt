package org.jetbrains.qodana.dart

import com.intellij.codeInspection.InspectionEP
import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.openapi.application.PluginPathManager
import com.intellij.openapi.extensions.DefaultPluginDescriptor
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.HeavyTestHelper
import com.jetbrains.qodana.sarif.model.Location
import com.jetbrains.qodana.sarif.model.Result
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.globalOutput.GlobalOutputConsumer
import org.jetbrains.qodana.staticAnalysis.profile.DerivedInspectionProvider
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfileProvider
import org.jetbrains.qodana.staticAnalysis.sarif.SarifReportContributor
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import org.jetbrains.qodana.staticAnalysis.testFramework.reinstantiateInspectionRelatedServices
import java.nio.file.Files
import java.nio.file.Path

internal abstract class DartAnalyzeQodanaTestCase : QodanaRunnerTestCase() {
  protected open val analyzeResponses: Map<String, RecordedDartAnalyzeResponse> = emptyMap()
  private lateinit var analyzerExecutions: MutableList<Pair<String, String>>
  private val derivedInspectionsDisposable = Disposer.newDisposable()

  override val testData: Path = Path.of(PluginPathManager.getPluginHomePath("qodana"), "dart", "test-data")

  override fun setUp() {
    super.setUp()
    updateQodanaConfig { it.copy(profile = QodanaProfileConfig.named(DART_STARTER_PROFILE)) }
    Disposer.register(testRootDisposable, derivedInspectionsDisposable)
    DerivedInspectionProvider.EP_NAME.point.registerExtension(DartAnalyzeDerivedInspectionProvider(), derivedInspectionsDisposable)
    QodanaInspectionProfileProvider.EP_NAME.point.registerExtension(DartInspectionProfileProvider(), testRootDisposable)
    analyzerExecutions = mutableListOf()
    registerDartAnalyzeTool(createRunnerFactory(analyzerExecutions))
    GlobalOutputConsumer.EP_NAME.point.registerExtension(DartAnalyzeGlobalInspectionOutputConsumer(), testRootDisposable)
    ExtensionPointName.create<SarifReportContributor>("org.intellij.qodana.sarifReportContributor").point
      .registerExtension(DartAnalyzeGlobalInspectionOutputConsumer(), testRootDisposable)
    reinstantiateInspectionRelatedServices(project, testRootDisposable)
  }

  protected open fun createRunnerFactory(executions: MutableList<Pair<String, String>>): DartAnalyzeRunnerFactory =
    DartAnalyzeTestRunnerFactory(Path.of(project.basePath!!), analyzeResponses, executions).create()

  private fun registerDartAnalyzeTool(runnerFactory: DartAnalyzeRunnerFactory) {
    val inspection = object : InspectionEP() {
      override fun instantiateTool(): InspectionProfileEntry = DartAnalyzeGlobalInspection(runnerFactory).also {
        it.nameProvider = this
      }
    }
    inspection.implementationClass = DartAnalyzeGlobalInspection::class.java.name
    inspection.shortName = DART_ANALYZE_SHORT_NAME
    inspection.presentation = DartAnalyzeGlobalInspectionPresentation::class.java.name
    inspection.enabledByDefault = true
    inspection.pluginDescriptor = DefaultPluginDescriptor(PluginId.getId("qodanaTest"), javaClass.classLoader)
    InspectionEP.GLOBAL_INSPECTION.point.registerExtension(inspection, testRootDisposable)
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

  protected fun assertRecordedAnalysisDirectories(vararg directories: String) {
    assertEquals(
      "Unexpected Dart analysis directories",
      directories.toList().sorted(),
      analyzerExecutions.filter { it.first == "analyze" }.map { it.second }.sorted(),
    )
  }

  protected fun assertRecordedDartExecutions(vararg expected: Pair<String, String>) {
    assertEquals("Unexpected Dart executions", expected.toList(), analyzerExecutions)
  }

  protected fun disableDerivedInspections() {
    Disposer.dispose(derivedInspectionsDisposable)
  }
}

internal abstract class DartAnalyzeProjectQodanaTestCase : DartAnalyzeQodanaTestCase() {
  override val testDataBasePath: Path = Path.of(javaClass.simpleName)

  protected open val projectSourcePath: Path
    get() = testData.resolve(testDataBasePath).resolve("sources/test-module")

  override fun createTestStructure() {
    val projectRoot = Path.of(project.basePath!!)
    Files.createDirectories(projectRoot)
    HeavyTestHelper.createTestProjectStructure(myModule, projectSourcePath.toString(), projectRoot, true)
    PsiDocumentManager.getInstance(myProject).commitAllDocuments()
  }
}
