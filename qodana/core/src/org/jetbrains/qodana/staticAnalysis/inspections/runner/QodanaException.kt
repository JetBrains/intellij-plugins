package org.jetbrains.qodana.staticAnalysis.inspections.runner

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

open class QodanaException : RuntimeException {
  constructor(message: String, cause: Throwable) : super(message, cause)
  constructor(message: String) : super(message)
}

class QodanaCancellationException(message: String) : CancellationException(message)

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
