// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.progress.ProcessCanceledException
import kotlinx.coroutines.CancellationException
import org.jetbrains.qodana.util.QodanaMessageReporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression tests for the QD-15440 hang: [QodanaInspectionApplication.reportTerminalError] must handle any terminal
 * exception without rethrowing it, so the caller's `exitProcess` always runs.
 */
class QodanaTerminalErrorTest {
  private class RecordingReporter : QodanaMessageReporter {
    val errors = mutableListOf<Throwable>()
    val messages = mutableListOf<String?>()
    override fun reportError(e: Throwable) { errors += e }
    override fun reportError(message: String?) { messages += message }
    override fun reportMessage(minVerboseLevel: Int, message: String?) {}
  }

  private val reporter = RecordingReporter()

  // Reaching the return also proves reportTerminalError did not rethrow — the property that keeps exitProcess from being skipped.
  private fun report(e: Throwable) = QodanaInspectionApplication.reportTerminalError(e, reporter)

  @Test
  fun `a cancellation is reported as-is`() {
    // IndicatorCancellationException (the culprit) is package-private; a plain CancellationException is its supertype.
    val ce = CancellationException("simulated indicator cancellation")

    report(ce)

    assertEquals(listOf<Throwable>(ce), reporter.errors)
    assertTrue(reporter.messages.isEmpty())
  }

  @Test
  fun `a ProcessCanceledException is reported via its wrapped QodanaCancellationException reason`() {
    val reason = QodanaCancellationException("license expired")

    report(ProcessCanceledException(reason))

    assertEquals(listOf<Throwable>(reason), reporter.errors)
  }

  @Test
  fun `a QodanaException is reported with a readable message`() {
    report(QodanaException("boom"))

    assertTrue(reporter.errors.isEmpty())
    assertEquals(listOf("Qodana exited abnormally because: boom"), reporter.messages)
  }

  @Test
  fun `an unexpected error is reported`() {
    val generic = RuntimeException("kaboom")

    report(generic)

    assertEquals(listOf<Throwable>(generic), reporter.errors)
  }
}
