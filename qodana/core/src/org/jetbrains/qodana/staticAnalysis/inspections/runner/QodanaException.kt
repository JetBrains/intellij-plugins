package org.jetbrains.qodana.staticAnalysis.inspections.runner

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import org.jetbrains.qodana.QodanaBundle

open class QodanaException : RuntimeException {
  constructor(message: String, cause: Throwable) : super(message, cause)
  constructor(message: String) : super(message)
}

class QodanaCancellationException(message: String) : CancellationException(message)

class QodanaReportedFailureException(
  val exitCode: Int,
  val exitCodeDescription: String,
  cause: Throwable? = null,
) : RuntimeException(exitCodeDescription, cause) {
  companion object {
    fun packageCheckerHeadlessFailure(details: String, cause: Throwable? = null): QodanaReportedFailureException {
      return QodanaReportedFailureException(
        PACKAGE_CHECKER_EXIT_CODE,
        QodanaBundle.message("exit.package.checker.headless.failure", details),
        cause
      )
    }
  }
}

internal fun CoroutineScope.cancelWithQodanaException(message: String, cause: Throwable? = null) {
  val cancelException = QodanaCancellationException(message)
  if (cause != null) {
    cancelException.initCause(cause)
  }
  cancel(cancelException)
}

class QodanaTimeoutException : QodanaException {
  constructor(message: String) : super(message)
  constructor(message: String, cause: Throwable) : super(message, cause)
}
