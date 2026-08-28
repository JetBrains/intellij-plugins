// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.codeInspection.InspectionApplicationException
import com.intellij.openapi.progress.ProcessCanceledException
import kotlinx.coroutines.CancellationException
import org.jetbrains.qodana.QodanaBundle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression tests for a hang where the platform logger's rethrow of a control-flow exception skipped the caller's
 * `exitProcess`. Returning at all is what each of these proves: phrasing a terminal failure must never rethrow.
 */
class QodanaTerminalErrorTest {
  private fun shown(e: Throwable): String? = consoleMessage(e, memoryVerdict(e))

  private fun logged(e: Throwable): Pair<String, Throwable?> = logRecord(e, memoryVerdict(e))!!

  @Test
  fun `a cancellation is reported as-is`() {
    // IndicatorCancellationException (the culprit) is package-private; a plain CancellationException is its supertype.
    assertEquals("simulated indicator cancellation", shown(CancellationException("simulated indicator cancellation")))
  }

  @Test
  fun `a ProcessCanceledException is reported via its wrapped QodanaCancellationException reason`() {
    assertEquals("license expired", shown(ProcessCanceledException(QodanaCancellationException("license expired"))))
  }

  @Test
  fun `an argument or config error is reported as itself, not as a Qodana bug`() {
    val e = InspectionApplicationException("Directory 'nope' does not exist")

    assertEquals("Directory 'nope' does not exist", shown(e))
    // The one shape that logs nothing: there is no fault to record.
    assertNull(logRecord(e, memoryVerdict(e)))
  }

  @Test
  fun `a QodanaException is reported with a readable message, and keeps its trace in the log`() {
    val boom = QodanaException("boom")

    assertEquals("Qodana exited abnormally because: boom", shown(boom))
    assertEquals(boom, logged(boom).second)
  }

  @Test
  fun `an internal fault is reported as a Qodana bug, not as a stack trace`() {
    val message = shown(RuntimeException("kaboom")).orEmpty()

    assertTrue(message, message.contains("internal error"))
    assertTrue(message, message.contains("jb.gg/qodana-issue"))
    // The throwable identifies the fault, so it must precede the log pointer rather than land in its argument slot.
    assertTrue(message, message.indexOf("java.lang.RuntimeException: kaboom") < message.indexOf("idea.log"))
  }

  @Test
  fun `an internal fault with no message is identified by its class name, not by null`() {
    val message = shown(RuntimeException()).orEmpty()

    assertTrue(message, message.contains("internal error"))
    assertTrue(message, message.contains(RuntimeException::class.java.name))
    assertFalse(message, message.contains("null"))
  }

  @Test
  fun `an anonymous internal fault is still identified, though its simple name is empty`() {
    val e = object : RuntimeException("boom") {}
    // The pathological case this guards against: Class.getSimpleName() is contractually "" for an anonymous class.
    assertTrue(e.javaClass.simpleName, e.javaClass.simpleName.isEmpty())

    val message = shown(e).orEmpty()

    assertTrue(message, message.contains("internal error"))
    assertTrue(message, message.contains(e.javaClass.name))
  }

  @Test
  fun `an abort with no recorded reason is reported in our own words, with its trace in the log`() {
    // A bare ProcessCanceledException has no message, so rendering it would put its class name on the console. The
    // trace goes to the log as text: Logger.ensureNotControlFlow replaces a control-flow throwable with a synthetic one.
    val abort = ProcessCanceledException()

    assertEquals(QodanaBundle.message("cli.run.cancelled"), shown(abort))
    val (message, thrown) = logged(abort)
    assertNull(thrown)
    assertTrue(message, message.contains("\tat "))
  }
}
