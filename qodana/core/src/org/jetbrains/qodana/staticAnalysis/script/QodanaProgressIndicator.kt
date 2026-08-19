package org.jetbrains.qodana.staticAnalysis.script

import com.intellij.codeInspection.ex.JobDescriptor
import com.intellij.openapi.progress.util.ProgressIndicatorBase
import com.intellij.openapi.progress.util.ProgressIndicatorWithDelayedPresentation
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.staticAnalysis.inspections.runner.isInteractiveOutput
import org.jetbrains.qodana.staticAnalysis.inspections.runner.splitProgressText
import org.jetbrains.qodana.util.QodanaMessageReporter
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

private const val PROGRESS_LOG_INTERVAL_PROPERTY = "qodana.progress.log.interval.ms"
private val DEFAULT_PROGRESS_LOG_INTERVAL: Duration = 5.seconds

private fun loadProgressLogInterval(): Duration {
  val intervalMillis = System.getProperty(PROGRESS_LOG_INTERVAL_PROPERTY)
                         ?.toLongOrNull()
                         ?.takeIf { it >= 0 }
                       ?: return DEFAULT_PROGRESS_LOG_INTERVAL
  return intervalMillis.milliseconds
}

class QodanaProgressIndicator(
  internal val messageReporter: QodanaMessageReporter,
  internal val timeSource: TimeSource = TimeSource.Monotonic,
) : ProgressIndicatorBase(), ProgressIndicatorWithDelayedPresentation {
  internal val jobProgressReporters = ConcurrentHashMap<JobDescriptor, JobProgressReporter>()
  internal val progressLogInterval = loadProgressLogInterval()

  init {
    text = ""
  }

  fun reportPercentProgress(job: JobDescriptor, text: String) {
    jobProgressReporters.computeIfAbsent(job) {
      PercentJobProgressReporter(
        job = job,
        messageReporter = messageReporter,
        timeSource = timeSource,
        progressLogInterval = progressLogInterval,
      )
    }.reportProgress(text)
  }

  internal fun reportCounterProcess(job: JobDescriptor, text: String) {
    jobProgressReporters.computeIfAbsent(job) {
      CounterJobProgressReporter(
        job = job,
        messageReporter = messageReporter,
        timeSource = timeSource,
        progressLogInterval = progressLogInterval,
      )
    }.reportProgress(text)
  }

  override fun setDelayInMillis(delayInMillis: Int) {
  }

  internal abstract class JobProgressReporter(
    private val messageReporter: QodanaMessageReporter,
    private val timeSource: TimeSource,
    private val progressLogInterval: Duration,
  ) {
    private var lastLoggedValue = 0
    private var lastLoggedAt: TimeMark? = null

    @Synchronized
    fun reportProgress(text: String) {
      val value = progressValue()
      if (value == null || value == 0) return

      val monotonicValue = value.coerceAtLeast(lastLoggedValue)
      if (monotonicValue == lastLoggedValue) return
      if (!shouldForceReport(monotonicValue) && (lastLoggedAt?.elapsedNow() ?: Duration.INFINITE) < progressLogInterval) {
        return
      }

      messageReporter.reportMessage(2, formatMessage(text, monotonicValue))
      lastLoggedValue = monotonicValue
      lastLoggedAt = timeSource.markNow()
    }

    protected abstract fun progressValue(): Int?

    protected open fun shouldForceReport(value: Int): Boolean = value == 100

    protected open fun formatMessage(text: String, value: Int): String {
      val (prefix, file) = splitProgressText(text)
      return if (isInteractiveOutput() && file != null) "$prefix $value% [$file]" else "$prefix $value%"
    }
  }

  private class PercentJobProgressReporter(
    private val job: JobDescriptor,
    messageReporter: QodanaMessageReporter,
    timeSource: TimeSource,
    progressLogInterval: Duration,
  ) : JobProgressReporter(
    messageReporter = messageReporter,
    timeSource = timeSource,
    progressLogInterval = progressLogInterval,
  ) {
    override fun progressValue(): Int? {
      val totalAmount = job.totalAmount
      return if (totalAmount > 0) job.doneAmount * 100 / totalAmount else null
    }
  }

  internal class LocalAnalysisReporter(
    private val defaultTotalAmount: () -> Int,
    messageReporter: QodanaMessageReporter,
    timeSource: TimeSource,
    progressLogInterval: Duration,
  ) : JobProgressReporter(
    messageReporter = messageReporter,
    timeSource = timeSource,
    progressLogInterval = progressLogInterval,
  ) {
    private var completedFiles = 0

    @Volatile
    private var scheduledFilesCount: Int? = null

    fun setScheduledFilesCount(scheduledFilesCount: Int) {
      this.scheduledFilesCount = scheduledFilesCount
    }

    override fun progressValue(): Int? {
      val totalAmount = scheduledFilesCount ?: defaultTotalAmount()
      return if (totalAmount > 0) ++completedFiles * 100 / totalAmount else null
    }
  }

  private class CounterJobProgressReporter(
    private val job: JobDescriptor,
    messageReporter: QodanaMessageReporter,
    timeSource: TimeSource,
    progressLogInterval: Duration,
  ) : JobProgressReporter(
    messageReporter = messageReporter,
    timeSource = timeSource,
    progressLogInterval = progressLogInterval,
  ) {
    override fun progressValue(): Int = job.doneAmount

    override fun shouldForceReport(value: Int): Boolean = false

    override fun formatMessage(text: String, value: Int): String {
      return QodanaBundle.message(
        "progress.code.analysis.processed.files",
        splitProgressText(text).first,
        value,
      )
    }
  }
}
