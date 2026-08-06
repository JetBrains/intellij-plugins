package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.components.Service
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.qodana.QodanaBundle

private val alreadyRequestedCancellation = QodanaAnalysisCancellation { _, _ -> }

@ApiStatus.Internal
fun interface QodanaAnalysisCancellation {
  fun cancel(message: String, cause: Throwable?)
}

@ApiStatus.Internal
@Service(Service.Level.PROJECT)
class QodanaAnalysisCancellationService {
  private var cancellation: QodanaAnalysisCancellation? = null
  private var logger: ((String) -> Unit)? = null
  private var warningLogged = false

  fun registerHook(cancellation: QodanaAnalysisCancellation) {
    registerHook(cancellation, null)
  }

  fun registerHook(cancellation: QodanaAnalysisCancellation, logger: ((String) -> Unit)?) {
    synchronized(this) {
      this.cancellation = cancellation
      this.logger = logger
      warningLogged = false
    }
  }

  fun removeHook() {
    synchronized(this) {
      cancellation = null
      logger = null
      warningLogged = false
    }
  }

  fun requestCancel(message: String, cause: Throwable? = null): Boolean {
    val currentCancellation = synchronized(this) {
      val currentCancellation = cancellation ?: return false
      if (currentCancellation === alreadyRequestedCancellation) return true
      cancellation = alreadyRequestedCancellation
      currentCancellation
    }
    currentCancellation.cancel(message, cause)
    return true
  }

  fun logCancellationDisabledWarning() {
    val currentLogger = synchronized(this) {
      if (warningLogged) return
      val currentLogger = logger ?: return
      warningLogged = true
      currentLogger
    }
    currentLogger(QodanaBundle.message("warning.package.checker.failure"))
  }
}
