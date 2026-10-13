package org.jetbrains.qodana.staticAnalysis.diogen

import com.intellij.diagnostic.ITNReporter
import com.intellij.diagnostic.toProblematicPluginInfo
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.ide.plugins.PluginUtils
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.openapi.diagnostic.UnhandledExceptionKind
import com.intellij.openapi.diagnostic.logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaConfigurationException
import org.jetbrains.qodana.staticAnalysis.inspections.runner.memoryVerdict
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

internal val DIOGEN_REPORT_DRAIN_TIMEOUT: Duration = 30.seconds

internal class QodanaDiogenStatistics {
  val sent = AtomicInteger()
  val failures = AtomicInteger()
  val duplicates = AtomicInteger()
  val cancellations = AtomicInteger()
  val totalSendTimeMs = AtomicLong()

  private val averageSendTimeMs: Long
    get() {
      val submissions = sent.get() + failures.get()
      return if (submissions == 0) 0 else totalSendTimeMs.get() / submissions
    }

  override fun toString(): String =
    "sent=$sent, failures=$failures, duplicates=$duplicates, cancellations=$cancellations, averageSendTime=${averageSendTimeMs}ms"
}

@Service(Service.Level.APP)
internal class QodanaDiogenReporter @JvmOverloads constructor(
  private val coroutineScope: CoroutineScope,
  private val dispatcher: CoroutineDispatcher? = Dispatchers.IO,
  private val sendAction: suspend (IdeaLoggingEvent) -> SubmittedReportInfo = ITNReporter()::submitAutomated,
) {
  companion object {
    private val LOG = logger<QodanaDiogenReporter>()
  }

  private val lock = ReentrantLock()
  private val reportedStackTraceHashes = ConcurrentHashMap.newKeySet<UUID>()
  private val pending = ConcurrentHashMap.newKeySet<Job>()

  //Diogen team recommends max 2 simultaneous HTTP requests
  private val submissionPermits = Semaphore(2)

  @Volatile
  private var acceptsReports = false
  internal val statistics = QodanaDiogenStatistics()

  fun start(enabled: Boolean) {
    acceptsReports = enabled
    LOG.info("Qodana Diogen reporting started: enabled=$enabled")
  }

  fun reportInspectionFailure(toolId: String, throwable: Throwable) = report("Inspection $toolId failed", throwable)

  fun reportApplicationCrash(throwable: Throwable) = report("Qodana failed with a terminal error", throwable)

  private fun report(message: String, throwable: Throwable) {
    if (!acceptsReports ||
        throwable is CancellationException ||
        throwable is QodanaConfigurationException ||
        memoryVerdict(throwable).holdsAnyMemoryError) {
      return
    }
    val plugin = PluginUtils.findPlugin(throwable, PluginManagerCore.getPluginSetOrNull())?.second
    if (plugin != null && !PluginManagerCore.isDevelopedByJetBrains(plugin)) return
    val stackTraceHash = UUID.nameUUIDFromBytes(throwable.stackTraceToString().toByteArray())
    if (!reportedStackTraceHashes.add(stackTraceHash)) {
      statistics.duplicates.incrementAndGet()
      return
    }

    val event = IdeaLoggingEvent(message, throwable, emptyList(), toProblematicPluginInfo(plugin), null, UnhandledExceptionKind.HANDLED)
    val job = lock.withLock {
      if (!acceptsReports) return
      coroutineScope.launch(dispatcher ?: EmptyCoroutineContext) { send(event) }.also { pending += it }
    }
    job.invokeOnCompletion { pending.remove(job) }
  }

  private suspend fun send(event: IdeaLoggingEvent) {
    val submission = submissionPermits.withPermit {
      val sendingStartedAt = TimeSource.Monotonic.markNow()
      sendAction(event).also { statistics.totalSendTimeMs.addAndGet(sendingStartedAt.elapsedNow().inWholeMilliseconds) }
    }
    currentCoroutineContext().ensureActive()

    if (submission.status == SubmittedReportInfo.SubmissionStatus.FAILED) {
      statistics.failures.incrementAndGet()
      LOG.warn("Qodana Diogen report was not sent: ITNReporter Submission Status is: FAILED")
    }
    else {
      statistics.sent.incrementAndGet()
      LOG.info("Qodana Diogen report sent: report ID: ${submission.linkText}")
    }
  }

  suspend fun stop() {
    val jobs = lock.withLock {
      if (!acceptsReports) return
      acceptsReports = false
      pending.toList()
    }
    val drained = withTimeoutOrNull(DIOGEN_REPORT_DRAIN_TIMEOUT) {
      jobs.joinAll()
      true
    } == true
    if (!drained) {
      val jobsToCancel = jobs.filter(Job::isActive)
      statistics.cancellations.addAndGet(jobsToCancel.size)
      LOG.warn("Qodana Diogen drain timed out; cancelling ${jobsToCancel.size} report(s)")
      jobsToCancel.forEach(Job::cancel)
    }

    LOG.info("Qodana Diogen reporting statistics: $statistics")
  }
}
