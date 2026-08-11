// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.progress.ProcessCanceledException
import kotlinx.coroutines.CancellationException
import org.jetbrains.qodana.util.QodanaMessageReporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
  fun `an internal fault is reported as a Qodana bug, not as a stack trace`() {
    report(RuntimeException("kaboom"))

    assertEquals(emptyList<Throwable>(), reporter.errors)
    val message = soleMessage()
    assertTrue(message, message.contains("internal error"))
    assertTrue(message, message.contains("jb.gg/qodana-issue"))
    // The throwable identifies the fault, so it must precede the log pointer rather than land in its argument slot.
    assertTrue(message, message.indexOf("java.lang.RuntimeException: kaboom") < message.indexOf("idea.log"))
  }

  @Test
  fun `an Error is reported as a Qodana bug`() {
    // A failed assert or a broken linter image is our defect.
    report(AssertionError("invariant broken"))

    assertEquals(emptyList<Throwable>(), reporter.errors)
    val message = soleMessage()
    assertTrue(message, message.contains("internal error"))
    assertTrue(message, message.contains("jb.gg/qodana-issue"))
  }

  @Test
  fun `a throwable without a message is still named`() {
    report(RuntimeException())

    assertTrue(soleMessage(), soleMessage().contains("java.lang.RuntimeException"))
    assertFalse(soleMessage(), soleMessage().contains("null"))
  }

  @Test
  fun `an anonymous throwable is still named`() {
    report(object : RuntimeException("kaboom") {})

    // getSimpleName() is empty for an anonymous class, which would render a bare leading colon.
    assertFalse(soleMessage(), soleMessage().contains(": : "))
    assertTrue(soleMessage(), soleMessage().contains("kaboom"))
  }

  private fun soleMessage(): String = reporter.messages.single().orEmpty()
}
