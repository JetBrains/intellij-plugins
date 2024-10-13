package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.ide.CommandLineInspectionProgressReporter

interface QodanaMessageReporter : CommandLineInspectionProgressReporter {
  fun reportError(e: Throwable)

  override fun reportError(message: String?)

  override fun reportMessage(minVerboseLevel: Int, message: String?)

  fun reportMessageNoLineBreak(minVerboseLevel: Int, message: String?)

  object EMPTY : QodanaMessageReporter {
    override fun reportError(e: Throwable) {}

    override fun reportError(message: String?) {}

    override fun reportMessage(minVerboseLevel: Int, message: String?) {}

    override fun reportMessageNoLineBreak(minVerboseLevel: Int, message: String?) {}
  }

  object DEFAULT : QodanaMessageReporter {
    private const val VERBOSITY = 3

    override fun reportError(e: Throwable) {
      reportError(e.stackTraceToString())
    }

    override fun reportError(message: String?) {
      System.err.println(message)
    }

    override fun reportMessage(minVerboseLevel: Int, message: String?) {
      if (VERBOSITY >= minVerboseLevel) println(message)
    }

    override fun reportMessageNoLineBreak(minVerboseLevel: Int, message: String?) {
      if (VERBOSITY >= minVerboseLevel) print(message)
    }
  }
}
