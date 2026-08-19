package org.jetbrains.qodana.staticAnalysis.script

import com.intellij.codeInspection.ex.JobDescriptor
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
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class QodanaProgressIndicatorTest : QodanaTestCase() {
  @Test
  fun `indicator logs only local analysis progress once per percent`() {
    val reporter = RecordingMessageReporter()
    val indicator = QodanaProgressIndicator(reporter)
    val job = JobDescriptor("Analyzing code")

    indicator.reportPercentProgress(job, "Analyzing code in src/A.kt", 0)
    indicator.reportPercentProgress(job, "Analyzing code in src/D.kt", 42)
    indicator.reportPercentProgress(job, "Analyzing code in src/D.kt", 42)
    indicator.reportPercentProgress(job, "Analyzing code in src/E.kt", 100)

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
    assertThat(reporter.messages).contains("${buildGraph.displayName.removeSuffix(" in")}: 1 files")
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

  @Test
  fun `indicator logs progress independently for jobs with the same phase text and never decreases`() {
    val reporter = RecordingMessageReporter()
    val indicator = QodanaProgressIndicator(reporter)
    val firstJob = JobDescriptor("Phase")
    val secondJob = JobDescriptor("Phase")

    indicator.reportPercentProgress(firstJob, "Phase", 50)
    indicator.reportPercentProgress(secondJob, "Phase", 40)
    indicator.reportPercentProgress(firstJob, "Phase", 33)
    indicator.reportPercentProgress(firstJob, "Phase", 100)

    assertThat(reporter.messages).containsExactly(
      "Phase 50%",
      "Phase 40%",
      "Phase 100%",
    )
  }

  @Test
  fun `context reports build graph files and external usages progress independently`() = runTest {
    val reporter = RecordingMessageReporter()
    val context = createContext(this)
    context.progressIndicator = QodanaProgressIndicator(reporter)
    val buildGraph = context.stdJobDescriptors.BUILD_GRAPH
    buildGraph.totalAmount = 2
    val externalUsages = context.stdJobDescriptors.FIND_EXTERNAL_USAGES
    externalUsages.totalAmount = 2

    context.incrementJobDoneAmount(buildGraph, "in src/A.kt")
    context.incrementJobDoneAmount(externalUsages, "in src/A.kt")

    assertThat(reporter.messages).containsExactly(
      "${buildGraph.displayName.removeSuffix(" in")}: 1 files",
      "${externalUsages.displayName.removeSuffix(" in")} 50%",
    )
  }

  @Test
  fun `indicator throttles processed files without treating 100 files as completion`() {
    val timeSource = TestTimeSource()
    val reporter = RecordingMessageReporter()
    val indicator = QodanaProgressIndicator(reporter, timeSource)
    val job = JobDescriptor("Processing project usages in")

    job.doneAmount = 1
    indicator.reportCounterProcess(job, "Processing project usages in src/A.kt")
    timeSource += 4.seconds
    job.doneAmount = 100
    indicator.reportCounterProcess(job, "Processing project usages in src/B.kt")
    timeSource += 1.seconds
    job.doneAmount = 101
    indicator.reportCounterProcess(job, "Processing project usages in src/C.kt")

    assertThat(reporter.messages).containsExactly(
      "Processing project usages: 1 files",
      "Processing project usages: 101 files",
    )
  }

  @Test
  fun `indicator throttles percent progress within a phase`() {
    val timeSource = TestTimeSource()
    val reporter = RecordingMessageReporter()
    val indicator = QodanaProgressIndicator(reporter, timeSource)
    val job = JobDescriptor("Phase A")

    indicator.reportPercentProgress(job, "Phase A", 10)
    timeSource += 4.seconds
    indicator.reportPercentProgress(job, "Phase A", 20)
    timeSource += 1.seconds
    indicator.reportPercentProgress(job, "Phase A", 30)

    assertThat(reporter.messages).containsExactly(
      "Phase A 10%",
      "Phase A 30%",
    )
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

private fun QodanaProgressIndicator.reportPercentProgress(job: JobDescriptor, text: String, percent: Int) {
  job.totalAmount = 100
  job.doneAmount = percent
  reportPercentProgress(job, text)
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
  private var localAnalysisReporter: QodanaProgressIndicator.LocalAnalysisReporter? = null

  fun reportLocalAnalysisFilesScheduled(totalScheduledFiles: Int) {
    localAnalysisReporter?.setScheduledFilesCount(totalScheduledFiles)
    onScheduledFilesCounted(totalScheduledFiles)
  }

  var progressIndicator: ProgressIndicator
    get() = myProgressIndicator
    set(value) {
      myProgressIndicator = value
      (value as? QodanaProgressIndicator)?.let { progressIndicator ->
        val reporter = QodanaProgressIndicator.LocalAnalysisReporter(
          { stdJobDescriptors.LOCAL_ANALYSIS.totalAmount },
          progressIndicator.messageReporter,
          progressIndicator.timeSource,
          progressIndicator.progressLogInterval,
        )
        localAnalysisReporter = reporter
        progressIndicator.jobProgressReporters.putIfAbsent(
          stdJobDescriptors.LOCAL_ANALYSIS,
          reporter,
        )
      }
    }
}
