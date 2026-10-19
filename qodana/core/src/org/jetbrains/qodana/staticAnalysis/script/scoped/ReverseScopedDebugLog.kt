package org.jetbrains.qodana.staticAnalysis.script.scoped

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger

/**
 * Debug log of the reverse-scoped baseline comparison, of the result summary and of the exit status.
 *
 * It is off by default.
 * To turn it on, add `#org.jetbrains.qodana.staticAnalysis.script.scoped.ReverseScopedDebugLog` to `idea.log.debug.categories`.
 */
internal object ReverseScopedDebugLog {
  private val LOG = logger<ReverseScopedDebugLog>()

  val isEnabled: Boolean
    get() = LOG.isDebugEnabled

  fun log(message: () -> String) {
    LOG.debug { message() }
  }
}
