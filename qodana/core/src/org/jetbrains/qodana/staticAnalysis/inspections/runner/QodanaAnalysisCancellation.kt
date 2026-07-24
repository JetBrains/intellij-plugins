package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.components.Service
import org.jetbrains.annotations.ApiStatus

private val alreadyRequestedCancellation = QodanaAnalysisCancellation { _, _ -> }

@ApiStatus.Internal
fun interface QodanaAnalysisCancellation {
  fun cancel(message: String, cause: Throwable?)
}

@ApiStatus.Internal
@Service(Service.Level.PROJECT)
class QodanaAnalysisCancellationService {
  private var cancellation: QodanaAnalysisCancellation? = null

  fun registerHook(cancellation: QodanaAnalysisCancellation) {
    synchronized(this) {
      this.cancellation = cancellation
    }
  }

  fun removeHook() {
    synchronized(this) {
      cancellation = null
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
}
