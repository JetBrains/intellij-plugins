package org.jetbrains.qodana.inspectionKts.mcp.impl

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.diagnostic.rethrowControlFlowException
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.fileLogger
import com.intellij.openapi.project.Project
import com.jetbrains.qodana.sarif.model.Result
import com.jetbrains.qodana.sarif.model.SarifReport
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaInspectionProfileLoader
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunContext
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.LoadedProfile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import org.jetbrains.qodana.staticAnalysis.sarif.createSarifReport
import org.jetbrains.qodana.staticAnalysis.sarif.fillComponents
import org.jetbrains.qodana.staticAnalysis.sarif.getOrCreateRun
import org.jetbrains.qodana.staticAnalysis.scopes.QodanaAnalysisScope
import org.jetbrains.qodana.staticAnalysis.script.populateRunWithInspectionResults
import org.jetbrains.qodana.staticAnalysis.script.runAnalysis
import org.jetbrains.qodana.util.QodanaMessageReporter
import org.jetbrains.annotations.TestOnly
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.createDirectories
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/** Batches concurrent inspection requests into one project analysis. */
@Service(Service.Level.PROJECT)
internal class DynamicInspectionRunnerParallel private constructor(
  private val coroutineScope: CoroutineScope,
  private val analysisExecutor: AnalysisExecutor,
) {
  @Suppress("unused")
  constructor(project: Project, coroutineScope: CoroutineScope) : this(coroutineScope, QodanaAnalysisExecutor(project))

  private data class Submission(
    val tool: LocalInspectionTool,
    val config: QodanaConfig,
    val outputDirectory: Path,
    val timeout: Duration,
    val result: CompletableDeferred<List<Result>>,
  )

  private val mutex = Mutex()
  private val pendingSubmissions = mutableListOf<Submission>()
  private val submissionsByInspectionId = mutableMapOf<String, Submission>()
  private var workerJob: Job? = null
  private val batchCounter = AtomicInteger()

  suspend fun runSingle(
    tool: LocalInspectionTool,
    config: QodanaConfig,
    outputDirectory: Path,
    timeout: Duration = configuredTimeout(),
  ): List<Result> {
    val submission = mutex.withLock {
      if (submissionsByInspectionId.containsKey(tool.shortName)) {
        throw InspectionAnalysisConflictException(tool.shortName)
      }
      val created = Submission(tool, config, outputDirectory, timeout, CompletableDeferred())
      submissionsByInspectionId[tool.shortName] = created
      pendingSubmissions += created
      if (workerJob?.isActive != true) {
        workerJob = coroutineScope.launch { drainSubmissions() }
      }
      created
    }
    return try {
      withTimeout(timeout) { submission.result.await() }
    }
    catch (e: TimeoutCancellationException) {
      currentCoroutineContext().ensureActive()
      val removed = mutex.withLock {
        if (pendingSubmissions.remove(submission)) {
          submissionsByInspectionId.remove(submission.tool.shortName)
          true
        }
        else {
          false
        }
      }
      if (removed) {
        submission.result.completeExceptionally(
          InspectionAnalysisTimeoutException(timeout, "The queued inspection did not start before the time limit.", e)
        )
      }
      throw InspectionAnalysisTimeoutException(timeout, "The inspection did not finish before the time limit.", e)
    }
  }

  private suspend fun drainSubmissions() {
    while (true) {
      val batch = mutex.withLock {
        if (pendingSubmissions.isEmpty()) {
          workerJob = null
          null
        }
        else {
          pendingSubmissions.toList().also { pendingSubmissions.clear() }
        }
      } ?: return
      try {
        runBatch(batch)
      }
      finally {
        withContext(NonCancellable) {
          mutex.withLock {
            batch.forEach { submission ->
              if (submissionsByInspectionId[submission.tool.shortName] === submission) {
                submissionsByInspectionId.remove(submission.tool.shortName)
              }
            }
          }
        }
      }
    }
  }

  private suspend fun runBatch(batch: List<Submission>) {
    val firstSubmission = batch.first()
    val batchId = batchCounter.incrementAndGet()
    val batchDirectory = firstSubmission.outputDirectory.resolve("batched-analysis").resolve("batch-$batchId")
    try {
      batchDirectory.createDirectories()
      val resultsByRule = withTimeout(batch.minOf(Submission::timeout)) {
        analysisExecutor.run(batch.map(Submission::tool), firstSubmission.config)
      }
      batch.forEach { submission ->
        submission.result.complete(resultsByRule[submission.tool.shortName].orEmpty())
      }
    }
    catch (e: TimeoutCancellationException) {
      completeBatchFailure(batch, batchId, batchDirectory, "reached its time limit", e, batch.minOf(Submission::timeout))
    }
    catch (e: CancellationException) {
      if (currentCoroutineContext().isActive) {
        completeBatchFailure(batch, batchId, batchDirectory, "failed", e)
      }
      else {
        failEveryWaiter(batch, e)
        throw e
      }
    }
    catch (e: Throwable) {
      rethrowControlFlowException(e)
      completeBatchFailure(batch, batchId, batchDirectory, "failed", e)
    }
  }

  private suspend fun failEveryWaiter(batch: List<Submission>, cause: CancellationException) {
    val failure = IllegalStateException("The shared analysis stopped before it produced results.", cause)
    batch.forEach { it.result.completeExceptionally(failure) }
    withContext(NonCancellable) {
      mutex.withLock {
        pendingSubmissions.forEach { it.result.completeExceptionally(failure) }
        pendingSubmissions.clear()
        submissionsByInspectionId.clear()
      }
    }
  }

  private fun completeBatchFailure(
    batch: List<Submission>,
    batchId: Int,
    batchDirectory: Path,
    outcome: String,
    cause: Throwable,
    timedOutAfter: Duration? = null,
  ) {
    val inspectionIds = batch.map { it.tool.shortName }.sorted()
    batch.forEach { submission ->
      val message = "Inspection '${submission.tool.shortName}' did not complete. Shared batch $batchId $outcome. Batch output: $batchDirectory"
      submission.result.completeExceptionally(
        if (timedOutAfter == null) IllegalStateException(message, cause)
        else InspectionAnalysisTimeoutException(timedOutAfter, message, cause)
      )
    }
    fileLogger().warn("Shared inspection batch $batchId $outcome for ${inspectionIds.joinToString()}", cause)
  }

  internal companion object {
    const val TIMEOUT_PROPERTY: String = "qodana.mcp.default.project.analysis.timeout.minutes"
    const val DEFAULT_TIMEOUT_MINUTES: Long = 40

    fun configuredTimeout(): Duration =
      System.getProperty(TIMEOUT_PROPERTY)?.toLongOrNull()?.minutes ?: DEFAULT_TIMEOUT_MINUTES.minutes

    @TestOnly
    fun createForTest(
      coroutineScope: CoroutineScope,
      runAnalysis: suspend (Collection<LocalInspectionTool>, QodanaConfig) -> Map<String?, List<Result>>,
    ): DynamicInspectionRunnerParallel = DynamicInspectionRunnerParallel(
      coroutineScope,
      AnalysisExecutor { tools, config -> runAnalysis(tools, config) },
    )
  }
}

private fun interface AnalysisExecutor {
  suspend fun run(tools: Collection<LocalInspectionTool>, config: QodanaConfig): Map<String?, List<Result>>
}

private class QodanaAnalysisExecutor(private val project: Project) : AnalysisExecutor {
  override suspend fun run(
    tools: Collection<LocalInspectionTool>,
    config: QodanaConfig,
  ): Map<String?, List<Result>> = runWithFreshContext(tools, config)
    .report.getOrCreateRun().results.orEmpty().groupBy { it.ruleId }

  private suspend fun runWithFreshContext(
    tools: Collection<LocalInspectionTool>,
    config: QodanaConfig,
  ): AnalysisResult {
    val scope = QodanaAnalysisScope.fromConfigOrDefault(config, project, onPathNotFound = {})
    return supervisorScope {
      val profile = LoadedProfile.load(config, project, messageReporter = QodanaMessageReporter.EMPTY)
      val qodanaProfile = QodanaProfile.create(project, profile.profile, QodanaInspectionProfileLoader(project), config, false, false)
      val runContext = QodanaRunContext(project, profile, scope, qodanaProfile, config, this, QodanaMessageReporter.EMPTY)
      val result = runToolsOnContext(runContext, tools, config)
      coroutineContext.job.cancelChildren()
      result
    }
  }

  private suspend fun runToolsOnContext(
    runContext: QodanaRunContext,
    tools: Collection<LocalInspectionTool>,
    config: QodanaConfig,
  ): AnalysisResult = service<ExtractedInspectionsService>().withInspections(tools) {
    analyzeTools(runContext, tools, config)
  }

  private suspend fun analyzeTools(
    runContext: QodanaRunContext,
    tools: Collection<LocalInspectionTool>,
    config: QodanaConfig,
  ): AnalysisResult {
    val profile = LoadedProfile.load(config, project, messageReporter = QodanaMessageReporter.EMPTY)
    profile.profile.apply {
      initInspectionTools(project)
      disableAllTools(project)
      tools.forEach { setToolEnabled(it.shortName, true, project) }
    }

    val report = createSarifReport(emptyList())
    val qodanaProfile = QodanaProfile.create(project, profile.profile, QodanaInspectionProfileLoader(project), config, false, false)
    val inspectionContext = runContext.createGlobalInspectionContext(profile = qodanaProfile)
    try {
      runContext.runAnalysis(context = inspectionContext)
      val run = report.getOrCreateRun()
      fillComponents(run.tool, qodanaProfile)
      inspectionContext.populateRunWithInspectionResults(run)
    }
    finally {
      withContext(NonCancellable) { inspectionContext.closeQodanaContext() }
    }
    return AnalysisResult(report)
  }
}

private data class AnalysisResult(val report: SarifReport)

internal class InspectionAnalysisTimeoutException(
  bound: Duration,
  message: String,
  cause: Throwable,
) : Exception("$message Time limit: $bound.", cause)

private class InspectionAnalysisConflictException(shortName: String) : Exception(
  "Rule id '$shortName' is already in another analysis."
)
