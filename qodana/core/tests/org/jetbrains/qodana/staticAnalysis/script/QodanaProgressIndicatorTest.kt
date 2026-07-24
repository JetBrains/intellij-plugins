package org.jetbrains.qodana.staticAnalysis.script

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.ui.content.ContentManager
import kotlinx.coroutines.CoroutineScope
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.CoverageStatisticsData
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.QodanaCoverageComputationState
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import org.jetbrains.qodana.util.QodanaMessageReporter
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path

class QodanaProgressIndicatorTest : QodanaTestCase() {
  @Test
  fun `indicator logs only local analysis progress once per percent`() {
    val reporter = RecordingMessageReporter()
    val indicator = QodanaProgressIndicator(reporter)

    indicator.reportLocalAnalysisProgress("Analyzing code in src/A.kt", 0)
    indicator.reportLocalAnalysisProgress("Analyzing code in src/D.kt", 42)
    indicator.reportLocalAnalysisProgress("Analyzing code in src/D.kt", 42)
    indicator.reportLocalAnalysisProgress("Analyzing code in src/E.kt", 100)

    assertThat(reporter.messages.map { it.substringBefore(" [") }).containsExactly(
      "Analyzing code 42%",
      "Analyzing code 100%",
    )
  }

  @Test
  fun `context logs local analysis progress independently from global progress`() = runTest {
    val reporter = RecordingMessageReporter()
    val indicator = QodanaProgressIndicator(reporter)
    indicator.isIndeterminate = false
    val config = qodanaConfig()
    val context = TestQodanaGlobalInspectionContext(
      project = project,
      config = config,
      outputPath = Files.createTempDirectory("qodana-progress-indicator-test"),
      profile = constructProfile(config),
      qodanaRunScope = this,
      coverageStatisticsData = CoverageStatisticsData(QodanaCoverageComputationState.DEFAULT, project),
    )
    context.progressIndicator = indicator

    val localAnalysis = context.stdJobDescriptors.LOCAL_ANALYSIS
    localAnalysis.totalAmount = 3
    val buildGraph = context.stdJobDescriptors.BUILD_GRAPH
    buildGraph.totalAmount = 3

    context.reportLocalAnalysisFilesScheduled(3)
    context.incrementJobDoneAmount(localAnalysis, "in src/A.kt")
    context.incrementJobDoneAmount(buildGraph, "in src/A.kt")
    context.incrementJobDoneAmount(localAnalysis, "in src/B.kt")
    context.incrementJobDoneAmount(localAnalysis, "in src/C.kt")

    assertThat(reporter.messages.filter { it.startsWith("Analyzing code ") }.map { it.substringBefore(" [") }).containsExactly(
      "Analyzing code 33%",
      "Analyzing code 100%",
    )
  }

  @Test
  fun `context switches to scheduled files count for local analysis progress`() = runTest {
    val reporter = RecordingMessageReporter()
    val indicator = QodanaProgressIndicator(reporter)
    indicator.isIndeterminate = false
    val context = createContext(this)
    context.progressIndicator = indicator

    val localAnalysis = context.stdJobDescriptors.LOCAL_ANALYSIS
    localAnalysis.totalAmount = 6

    context.incrementJobDoneAmount(localAnalysis, "in src/A.kt")
    context.reportLocalAnalysisFilesScheduled(3)
    context.incrementJobDoneAmount(localAnalysis, "in src/B.kt")
    context.incrementJobDoneAmount(localAnalysis, "in src/C.kt")

    assertThat(reporter.messages.filter { it.startsWith("Analyzing code ") }.map { it.substringBefore(" [") }).containsExactly(
      "Analyzing code 16%",
      "Analyzing code 100%",
    )
  }

  @Test
  fun `context logs no files found when local analysis queue is empty`() = runTest {
    val indicator = QodanaProgressIndicator(RecordingMessageReporter())
    val config = qodanaConfig()
    val context = TestQodanaGlobalInspectionContext(
      project = project,
      config = config,
      outputPath = Files.createTempDirectory("qodana-progress-indicator-test"),
      profile = constructProfile(config),
      qodanaRunScope = this,
      coverageStatisticsData = CoverageStatisticsData(QodanaCoverageComputationState.DEFAULT, project),
    )
    context.progressIndicator = indicator

    val stdout = captureStdout {
      context.reportLocalAnalysisFilesScheduled(0)
    }

    assertThat(stdout).contains("No files found for analysis")
  }

  private fun createContext(qodanaRunScope: CoroutineScope): TestQodanaGlobalInspectionContext {
    val config = qodanaConfig()
    return TestQodanaGlobalInspectionContext(
      project = project,
      config = config,
      outputPath = Files.createTempDirectory("qodana-progress-indicator-test"),
      profile = constructProfile(config),
      qodanaRunScope = qodanaRunScope,
      coverageStatisticsData = CoverageStatisticsData(QodanaCoverageComputationState.DEFAULT, project),
    )
  }

  private fun captureStdout(block: () -> Unit): String {
    val originalOut = System.out
    val captured = ByteArrayOutputStream()
    System.setOut(PrintStream(captured))
    try {
      block()
    }
    finally {
      System.out.flush()
      System.setOut(originalOut)
    }
    return captured.toString()
  }
}

private class RecordingMessageReporter : QodanaMessageReporter by QodanaMessageReporter.EMPTY {
  val messages = mutableListOf<String>()

  override fun reportMessage(minVerboseLevel: Int, message: String?) {
    if (message != null) {
      messages.add(message)
    }
  }
}

private class TestQodanaGlobalInspectionContext(
  project: Project,
  config: QodanaConfig,
  outputPath: Path,
  profile: QodanaProfile,
  qodanaRunScope: CoroutineScope,
  coverageStatisticsData: CoverageStatisticsData,
) : QodanaGlobalInspectionContext(
  project = project,
  contentManager = object : NotNullLazyValue<ContentManager>() {
    override fun compute(): ContentManager = throw UnsupportedOperationException("Should not be called in tests")
  },
  config = config,
  outputPath = outputPath,
  profile = profile,
  qodanaRunScope = qodanaRunScope,
  coverageStatisticsData = coverageStatisticsData,
) {
  fun reportLocalAnalysisFilesScheduled(totalScheduledFiles: Int) {
    onScheduledFilesCounted(totalScheduledFiles)
  }

  var progressIndicator: ProgressIndicator
    get() = myProgressIndicator
    set(value) {
      myProgressIndicator = value
    }
}
