package org.jetbrains.qodana.staticAnalysis.inspections.coverage

import com.intellij.coverage.CoverageLoadingListener
import com.intellij.coverage.CoverageLoadingResult
import com.intellij.coverage.FailedCoverageLoadingResult
import com.intellij.coverage.SuccessCoverageLoadingResult
import org.jetbrains.qodana.util.QodanaMessageReporter
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

const val PRINTED_EXCEPTION_LIMIT: Int = 10

class QodanaCoverageLoadingListener: CoverageLoadingListener {

  companion object {
    fun buildTooManyErrorMessage(filePath: String): String =
      "More errors than limit $PRINTED_EXCEPTION_LIMIT were reported for file $filePath. For all errors please see the idea.log file"
  }

  private val reporter = QodanaMessageReporter.DEFAULT
  private val reportedErrors = ConcurrentHashMap<File, AtomicInteger>()

  override fun coverageLoadingStarted(coverageFile: File) {
    reporter.reportMessage(1, "Started loading coverage from $coverageFile...")
  }

  override fun reportCoverageLoaded(result: CoverageLoadingResult, coverageFile: File) {
    when (result) {
      is FailedCoverageLoadingResult ->
        reportError(coverageFile, "Could not load coverage from file $coverageFile: ${result.reason}", result.exception)
      is SuccessCoverageLoadingResult -> reporter.reportMessage(1, "Coverage from file $coverageFile loaded successfully.")
    }
  }

  override fun reportCoverageLoadException(reason: String, coverageFile: File, e: Exception?) {
    val message = "The coverage data from $coverageFile may be loaded incorrectly because of: $reason"
    reportError(coverageFile, message, e)
  }

  private fun reportError(coverageFile: File, message: String, e: Exception? = null) {
    val problemCounter = reportedErrors.getOrPut(coverageFile) { AtomicInteger(0) }
    val currentProblemCount = problemCounter.incrementAndGet()
    if (currentProblemCount <= PRINTED_EXCEPTION_LIMIT) {
      reporter.reportError(message)
      e?.let { reporter.reportError(it) }
    }
    if (currentProblemCount == PRINTED_EXCEPTION_LIMIT + 1) {
      reporter.reportError(buildTooManyErrorMessage(coverageFile.toString()))
    }
  }
}