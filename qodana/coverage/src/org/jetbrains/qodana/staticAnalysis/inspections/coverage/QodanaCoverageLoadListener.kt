package org.jetbrains.qodana.staticAnalysis.inspections.coverage

import com.intellij.coverage.CoverageLoadListener
import com.intellij.coverage.FailedLoadCoverageResult
import com.intellij.coverage.LoadCoverageResult
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaMessageReporter
import com.intellij.openapi.diagnostic.logger
import java.io.File

private val LOG = logger<QodanaCoverageLoadListener>()

private data class ExceptionLocation(
  val coverageFilePath: String,
  val exceptionStackTrace: Array<StackTraceElement>
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ExceptionLocation

    if (coverageFilePath != other.coverageFilePath) return false
    if (!exceptionStackTrace.contentEquals(other.exceptionStackTrace)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = coverageFilePath.hashCode()
    result = 31 * result + exceptionStackTrace.contentHashCode()
    return result
  }
}

class QodanaCoverageLoadListener: CoverageLoadListener {

  private val reporter = QodanaMessageReporter.DEFAULT
  private val reportedErrors = mutableSetOf<ExceptionLocation>()

  override fun coverageLoadingStarted(coverageFile: File) {
    reporter.reportMessage(1, "Started loading coverage from $coverageFile...")
  }

  override fun reportCoverageLoaded(result: LoadCoverageResult, coverageFile: File) {
    if (result is FailedLoadCoverageResult) {
      LOG.error(result.message, result.exception)
      reporter.reportError(result.message)
    }
  }

  override fun reportCoverageLoadException(message: String, e: Exception, coverageFile: File) {
    LOG.warn(message, e)
    // To not spam same errors to output
    if (reportedErrors.add(ExceptionLocation(coverageFile.path, e.stackTrace))) {
      reporter.reportError(message)
    }
  }
}