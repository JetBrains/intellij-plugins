package org.jetbrains.qodana.staticAnalysis.script

import com.intellij.analysis.AnalysisBundle
import com.intellij.openapi.progress.util.ProgressIndicatorBase
import com.intellij.openapi.progress.util.ProgressIndicatorWithDelayedPresentation
import org.jetbrains.qodana.staticAnalysis.inspections.runner.isInteractiveOutput
import org.jetbrains.qodana.staticAnalysis.inspections.runner.splitProgressText
import org.jetbrains.qodana.util.QodanaMessageReporter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

private val LOCAL_ANALYSIS_PREFIX = splitProgressText("${AnalysisBundle.message("inspection.processing.job.descriptor2")} sample").first
private const val PROGRESS_LOG_INTERVAL_PROPERTY = "qodana.progress.log.interval.ms"
private val DEFAULT_LOCAL_ANALYSIS_LOG_INTERVAL: Duration = 5.seconds

private fun loadLocalAnalysisLogInterval(): Duration {
  val intervalMillis = System.getProperty(PROGRESS_LOG_INTERVAL_PROPERTY)
                         ?.toLongOrNull()
                         ?.takeIf { it >= 0 }
                       ?: return DEFAULT_LOCAL_ANALYSIS_LOG_INTERVAL
  return intervalMillis.milliseconds
}

class QodanaProgressIndicator(
  private val messageReporter: QodanaMessageReporter,
  private val timeSource: TimeSource = TimeSource.Monotonic,
) : ProgressIndicatorBase(), ProgressIndicatorWithDelayedPresentation {
  private var lastLoggedPercent = -1
  private var lastLoggedLocalAnalysisPercent = -1
  private var lastLoggedLocalAnalysisAt: TimeMark? = null
  private val localAnalysisLogInterval = loadLocalAnalysisLogInterval()

  init {
    text = ""
  }

  fun reportLocalAnalysisProgress(text: String, percent: Int) {
    if (percent == 0 || lastLoggedLocalAnalysisPercent == percent || !shouldLogLocalAnalysisProgress(percent)) {
      return
    }
    val (prefix, file) = splitProgressText(text)
    reportText(prefix, file, percent)
    lastLoggedLocalAnalysisPercent = percent
    lastLoggedLocalAnalysisAt = timeSource.markNow()
  }

  override fun setText(text: String?) {
    if (text.isNullOrBlank()) {
      return
    }
    val (prefix, file) = splitProgressText(text)
    if (prefix == LOCAL_ANALYSIS_PREFIX) {
      return
    }

    val percent = (fraction * 100).toInt()
    if (isInteractiveOutput() || !isIndeterminate && fraction > 0 && lastLoggedPercent != percent) {
      reportText(prefix, file, percent)
      lastLoggedPercent = percent
    }
  }

  private fun reportText(prefix: String, file: String?, percent: Int) {
    val msg = if (isInteractiveOutput() && file != null) "$prefix $percent% [$file]" else "$prefix $percent%"
    messageReporter.reportMessage(2, msg)
  }

  private fun shouldLogLocalAnalysisProgress(percent: Int): Boolean {
    if (percent == 100) {
      return true
    }
    val loggedAt = lastLoggedLocalAnalysisAt ?: return true
    return loggedAt.elapsedNow() >= localAnalysisLogInterval
  }

  override fun setDelayInMillis(delayInMillis: Int) {
  }
}
