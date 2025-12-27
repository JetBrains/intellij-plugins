package org.jetbrains.qodana.inspectionKts

/**
 * Interface for reporting messages and errors during inspection.kts initialization.
 */
interface InspectionKtsMessageReporter {
  fun reportError(message: String)
  fun reportError(e: Throwable)

  object EMPTY : InspectionKtsMessageReporter {
    override fun reportError(message: String) {}
    override fun reportError(e: Throwable) {}
  }

  object DEFAULT : InspectionKtsMessageReporter {
    override fun reportError(message: String) {
      System.err.println(message)
    }

    override fun reportError(e: Throwable) {
      reportError(e.stackTraceToString())
    }
  }
}
