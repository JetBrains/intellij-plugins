@file:Suppress("TestOnlyProblems")

package org.jetbrains.qodana.staticAnalysis.testFramework

import com.intellij.codeInspection.ex.InspectionProfileImpl
import com.intellij.openapi.Disposable
import com.intellij.openapi.extensions.LoadingOrder
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.Disposer
import com.jetbrains.qodana.sarif.SarifUtil
import com.jetbrains.qodana.sarif.model.PropertyBag
import com.jetbrains.qodana.sarif.model.Result
import com.jetbrains.qodana.sarif.model.Run
import com.jetbrains.qodana.sarif.model.SarifReport
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.staticAnalysis.QodanaEnvEmpty
import org.jetbrains.qodana.staticAnalysis.addQodanaEnvMock
import org.jetbrains.qodana.staticAnalysis.inspections.config.*
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaYamlReader.defaultConfigPath
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.coverageStats
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.codeQualityMetrics
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QDCloudLinterProjectApi
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaInspectionApplication
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaMessageReporter
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunner
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.LoadedProfile
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.PreconfiguredRunContextFactory
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfileProvider
import org.jetbrains.qodana.staticAnalysis.sarif.SARIF_AUTOMATION_GUID_PROPERTY
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension
import java.io.StringWriter
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

class QodanaTestManager {
  private lateinit var testData: TestData

  class TestData(
    val project: Project,
    val testRootDisposable: Disposable,
    val projectPath: Path,
    val outputPath: Path,
    val getTestDataPath: (String) -> Path
  )

  private var qodanaApp: QodanaInspectionApplication? = null
  private lateinit var qodanaConfig: QodanaConfig
  lateinit var qodanaRunner: QodanaRunner

  @Suppress("RAW_RUN_BLOCKING")
  fun setUp(testData_: TestData): QodanaConfig {
    testData = testData_
    InspectionProfileImpl.INIT_INSPECTIONS = true
    val yamlPath = defaultConfigPath(testData.getTestDataPath(""))
    val projectPath = testData_.project.guessProjectDir()?.toNioPath() ?: testData_.projectPath
    val yamlConfig = runBlocking {
      yamlPath?.let { path ->
        QodanaYamlReader.load(path).getOrThrow().withAbsoluteProfilePath(projectPath, yamlPath)
      } ?: QodanaYamlConfig.EMPTY_V1
    }

    setSystemProperties()
    mockEnv()

    val isProfileSpecifiedInYaml = yamlConfig.profile != QodanaProfileConfig()
    qodanaConfig = QodanaConfig.fromYaml(
      testData.projectPath,
      testData.outputPath,
      yamlFiles = yamlPath?.let { QodanaYamlFiles.noConfigDir(yamlPath) } ?: QodanaYamlFiles.noFiles(),
      yaml = yamlConfig,
      baseline = testData.getTestDataPath("baseline-results.sarif.json").takeIf { it.exists() }?.toString(),
      profile = if (isProfileSpecifiedInYaml) yamlConfig.profile else QodanaProfileConfig(testData.getTestDataPath("inspection-profile.xml").toString(), ""),
      disableSanityInspections = true,
      runPromoInspections = false,
      fixesStrategy = FixesStrategy.NONE
    )

    reinstantiateInspectionRelatedServices(testData.project, testData.testRootDisposable)
    updateQodanaConfig(testData.projectPath, testData.outputPath) { it }
    return qodanaConfig
  }

  private fun setSystemProperties() {
    System.setProperty(SARIF_AUTOMATION_GUID_PROPERTY, "tests")
    Disposer.register(testData.testRootDisposable) {
      System.clearProperty(SARIF_AUTOMATION_GUID_PROPERTY)
    }
  }

  private fun mockEnv() {
    addQodanaEnvMock(testData.testRootDisposable, object : QodanaEnvEmpty() {
      override val QODANA_REPORT_ID by value("tests")
    })
  }

  fun registerEmbeddedProfilesTestProvider() {
    QodanaInspectionProfileProvider.EP_NAME.point.registerExtension(
      QodanaEmbeddedProfilesTestProvider(testData.getTestDataPath),
      LoadingOrder.FIRST,
      testData.testRootDisposable
    )
  }

  fun updateQodanaConfig(projectPath: Path,
                         outputPath: Path,
                         configured: (config: QodanaConfig) -> QodanaConfig)
  : Pair<QodanaConfig, QodanaInspectionApplication> {
    qodanaConfig = configured(qodanaConfig).copy(
      projectPath = projectPath,
      outPath = outputPath
    )
    val newApp = QodanaInspectionApplication(qodanaConfig, qodanaApp?.projectApi)
    qodanaApp = newApp
    return Pair(qodanaConfig, newApp)
  }

  fun setQodanaCloudProjectApi(api: QDCloudLinterProjectApi?) {
    qodanaApp = QodanaInspectionApplication(qodanaConfig, api)
  }

  fun runAnalysis(project: Project, messageReporter: QodanaMessageReporter = QodanaMessageReporter.DEFAULT): QodanaRunner {
    return ProgressManager.getInstance().runProcess(
      Computable { runBlockingCancellable { doRunAnalysis(project, messageReporter) } },
      EmptyProgressIndicator()
    )
  }

  private suspend fun doRunAnalysis(project: Project, messageReporter: QodanaMessageReporter): QodanaRunner {
    return coroutineScope {
      QodanaWorkflowExtension.callAfterConfiguration(qodanaConfig, project)

      val loadedProfile = loadInspectionProfile(project)
      val runner = qodanaApp!!.constructQodanaRunner(
        PreconfiguredRunContextFactory(qodanaConfig, messageReporter, project, loadedProfile, this),
        messageReporter
      )
      qodanaRunner = runner
      qodanaApp!!.launchRunner(runner)
      coroutineContext.cancelChildren()
      runner
    }
  }

  suspend fun loadInspectionProfile(project: Project) = LoadedProfile.load(qodanaConfig, project, QodanaMessageReporter.DEFAULT)

  fun computeSarifResult(getTestDataPath: (String) -> Path): Pair<String, String> {
    val comparator = compareBy<Result> { it.ruleId }
      .thenBy { it.locations.getOrNull(0)?.physicalLocation?.artifactLocation?.uri }
      .thenBy { it.locations.getOrNull(0)?.physicalLocation?.region?.startLine }
      .thenBy { it.locations.getOrNull(0)?.physicalLocation?.region?.charOffset }

    val sarifRun = qodanaRunner.sarifRun
    val sortedMainResults = sarifRun.results.distinct().sortedWith(comparator)

    val sanityResultsKey = "qodana.sanity.results"
    val sortedSanityResults = sarifRun.properties!![sanityResultsKey]?.let { SarifUtil.readResultsFromObject(it) }?.distinct()?.sortedWith(comparator)

    val promoResultsKey = "qodana.promo.results"
    val sortedPromoResults = sarifRun.properties!![promoResultsKey]?.let { SarifUtil.readResultsFromObject(it) }?.distinct()?.sortedWith(comparator)

    val run = Run().withResults(sortedMainResults).withAutomationDetails(sarifRun.automationDetails)

    if (sortedSanityResults?.isNotEmpty() == true || sortedPromoResults?.isNotEmpty() == true) {
      run.properties = PropertyBag().apply {
        set(sanityResultsKey, sortedSanityResults)
        set(promoResultsKey, sortedPromoResults)
      }
    }
    if (sarifRun.coverageStats.isNotEmpty()) run.coverageStats = sarifRun.coverageStats
    if (sarifRun.codeQualityMetrics.isNotEmpty()) run.codeQualityMetrics = sarifRun.codeQualityMetrics

    val actualReport = SarifReport().withRuns(listOf(run))

    val writer = StringWriter()
    SarifUtil.writeReport(writer, actualReport)

    val expectedSarif = getTestDataPath("expected.sarif.json").absolutePathString()
    return Pair(writer.toString(), expectedSarif)
  }
}
