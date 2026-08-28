// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.codeInspection.InspectionApplicationException
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.testFramework.LoggedErrorProcessor
import com.intellij.util.ExceptionUtil
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.staticAnalysis.testFramework.RecordingMessageReporter
import org.jetbrains.qodana.staticAnalysis.testFramework.QODANA_LOG_CATEGORY
import org.jetbrains.qodana.staticAnalysis.testFramework.logRecordsFrom
import org.jetbrains.qodana.util.QodanaMessageReporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream

private class SimulatedHeapExhaustion(message: String = "simulated heap exhaustion") : OutOfMemoryError(message)

class QodanaOutOfMemoryTest {
  // Detection ==========

  @Test
  fun `exhaustion is recognised directly and through a wrapper`() {
    val wrapped = RuntimeException("analysis failed", IllegalStateException(SimulatedHeapExhaustion()))

    assertTrue(memoryVerdict(SimulatedHeapExhaustion()).isSoftLimit)
    assertTrue(memoryVerdict(wrapped).isSoftLimit)
  }

  @Test
  fun `exhaustion carried only as a suppressed exception is recognised`() {
    // Structured concurrency attaches a sibling coroutine's failure this way, beside the failure rather than under it.
    val primary = IOException("failed to write the report")
    primary.addSuppressed(SimulatedHeapExhaustion())

    assertTrue(memoryVerdict(primary).isSoftLimit)
  }

  @Test
  fun `an unrelated failure is not exhaustion`() {
    assertFalse(memoryVerdict(RuntimeException("kaboom")).isSoftLimit)
    // A recursion defect is ours to fix, so it stays on the internal-error branch.
    assertFalse(memoryVerdict(StackOverflowError()).isSoftLimit)
  }

  @Test
  fun `metaspace and direct-buffer exhaustion are recognised too`() {
    val directBuffers = SimulatedHeapExhaustion("Cannot reserve 8388608 bytes of direct buffer memory (allocated: 0, limit: 1048576)")

    assertTrue(memoryVerdict(SimulatedHeapExhaustion("Metaspace")).isSoftLimit)
    assertTrue(memoryVerdict(directBuffers).isSoftLimit)
  }

  @Test
  fun `a throwable with no message at all is heap exhaustion`() {
    // The exclusions read the message, so they must stay null-safe about it rather than claiming this as their own.
    assertTrue(memoryVerdict(object : OutOfMemoryError() {}).isSoftLimit)
  }

  @Test
  fun `a limit a bigger container does not relieve is not claimed`() {
    // Fixed reservations: -XX:ReservedCodeCacheSize and -XX:CompressedClassSpaceSize.
    assertFalse(memoryVerdict(object : VirtualMachineError("CodeCache is full. Compiler has been disabled.") {}).isSoftLimit)
    assertFalse(memoryVerdict(SimulatedHeapExhaustion("Compressed class space")).isSoftLimit)
  }

  @Test
  fun `a failed thread creation is treated as a possible memory limit`() {
    // More container memory often does relieve this one, unlike the fixed reservations above.
    val e = OutOfMemoryError("unable to create native thread: possibly out of memory or process/resource limits reached")

    assertTrue(memoryVerdict(e).isSoftLimit)
  }

  @Test
  fun `only a memory error can report a failed thread creation`() {
    // The message alone is not enough: an ordinary exception can use the same words for an unrelated limit.
    assertFalse(memoryVerdict(IllegalStateException("unable to create native thread pool for indexing")).isSoftLimit)
  }

  @Test
  fun `an oversized array is our defect, not the operators limit`() {
    // No limit the operator can raise fixes a structure too large to allocate.
    assertFalse(memoryVerdict(SimulatedHeapExhaustion("Requested array size exceeds VM limit")).isSoftLimit)
  }

  @Test
  fun `an exhausted verdict below the maximum still runs the walk, and survives a poisoned cause`() {
    // SOFT_LIMIT is a soft limit but not the ordering's maximum, so a HARD_LIMIT sibling could still be found beyond
    // it: the walk must run even though e is already a soft limit, and a cause read that throws must not escape
    // uncaught.
    val hostile = object : OutOfMemoryError() {
      override val cause: Throwable get() = throw IllegalStateException("must not escape")
    }

    assertTrue(memoryVerdict(hostile).isSoftLimit)
  }

  @Test
  fun `the fallback classification recognises exhaustion in the throwable that broke the search`() {
    val hostile = object : RuntimeException("hostile") {
      override val cause: Throwable get() = throw SimulatedHeapExhaustion()
    }

    assertEquals(MemoryVerdict.SOFT_LIMIT, memoryVerdict(hostile))
  }

  @Test
  fun `a throwable that cannot be read is not exhaustion, and does not throw`() {
    // The caller is a catch block that still owes its caller an exit code, so a search that cannot finish must answer.
    val hostile = object : RuntimeException("hostile") {
      override val cause: Throwable get() = throw IllegalStateException("no cause for you")
    }

    assertEquals(MemoryVerdict.NOT_APPLICABLE, memoryVerdict(hostile))
  }

  @Test
  fun `a message that cannot be read costs only its own node`() {
    // The classifier reads `message`, which is overridable. The exhaustion may be the node beside the unreadable one.
    val primary = object : RuntimeException("primary") {
      override val message: String get() = throw IllegalStateException("no message for you")
    }
    primary.addSuppressed(SimulatedHeapExhaustion("Java heap space"))

    assertTrue(memoryVerdict(primary).isSoftLimit)
  }

  @Test
  fun `classification walks the graph once`() {
    // `getSuppressed` is final, so only reads of `cause` can be counted; the platform's walk re-reads `getCause`
    // several times within one pass, which is why the bound is one walk's own count and not an absolute number.
    class CountingCause : RuntimeException("counting") {
      var reads: Int = 0
      override val cause: Throwable? get() { reads++; return null }
    }

    val oneWalk = CountingCause().also { ExceptionUtil.findCauseAndSuppressed(it, Throwable::class.java) }.reads
    val classified = CountingCause().also { memoryVerdict(it) }.reads

    assertTrue("$classified reads against $oneWalk for a single walk", classified <= oneWalk)
  }

  @Test
  fun `a fixed ceiling established before a failed walk survives it`() {
    // HARD_LIMIT is the ordering's maximum, so this returns before the walk — which would touch the poisoned
    // cause — ever runs.
    val hostile = object : OutOfMemoryError("Requested array size exceeds VM limit") {
      override val cause: Throwable get() = throw IllegalStateException("no cause for you")
    }

    assertEquals(MemoryVerdict.HARD_LIMIT, memoryVerdict(hostile))
  }

  @Test
  fun `a fixed ceiling beside a relievable one is still found, and wins`() {
    // A limit no memory relieves outranks one it does: the fixed ceiling must not be overridden by a relievable
    // answer found later in the walk.
    val ours = SimulatedHeapExhaustion("Requested array size exceeds VM limit")
    ours.addSuppressed(SimulatedHeapExhaustion("Java heap space"))

    assertEquals(MemoryVerdict.HARD_LIMIT, memoryVerdict(ours))
  }

  @Test
  fun `a fixed ceiling beyond a relievable one is still found, and wins`() {
    // The mirror case: the relievable answer comes first in the walk and reaches `exhausted`, but must not end the
    // walk before the fixed ceiling beside it is found and takes over.
    val ours = SimulatedHeapExhaustion("Java heap space")
    ours.addSuppressed(SimulatedHeapExhaustion("Requested array size exceeds VM limit"))

    assertEquals(MemoryVerdict.HARD_LIMIT, memoryVerdict(ours))
  }

  @Test
  fun `a single-node graph is classified without walking`() {
    // A childless node reads `cause` and `suppressed` exactly once either way, so those reads cannot tell the fast
    // path apart from a walk that happened to visit only one node. `ExceptionUtil.findCauseAndSuppressed` also adds
    // the node to a `LinkedHashSet`, which reads `hashCode` — the fast path never does, so a `hashCode` count of zero
    // is what actually proves the walk did not run.
    class CountingHashCode : RuntimeException("solo") {
      var hashCodeReads: Int = 0
      override fun hashCode(): Int { hashCodeReads++; return super.hashCode() }
    }
    val e = CountingHashCode()

    assertEquals(MemoryVerdict.NOT_APPLICABLE, memoryVerdict(e))
    assertEquals(0, e.hashCodeReads)
  }

  @Test
  fun `an unreadable message does not discard the type test`() {
    // Relievability is decided from `message` and can be defeated by it; the type test cannot fail, so it is guarded
    // separately and still answers.
    val hostile = object : OutOfMemoryError() {
      override val message: String get() = throw IllegalStateException("no message for you")
    }

    assertEquals(MemoryVerdict.HARD_LIMIT, memoryVerdict(hostile))
  }

  // The terminal handler ==========

  private val sink = ByteArrayOutputStream()
  private val reporter = RecordingMessageReporter()
  private fun written() = sink.toString(Charsets.UTF_8)

  private fun handle(block: suspend () -> Unit): Int = runBlocking { runReportingTerminalFailure(reporter, sink, block) }

  private class RenderingMessageReporter : QodanaMessageReporter {
    val rendered = mutableListOf<String?>()
    override fun reportError(e: Throwable) { rendered += e.stackTraceToString() }
    override fun reportError(message: String?) { rendered += message }
    override fun reportMessage(minVerboseLevel: Int, message: String?) {}
  }

  private class ThrowingMessageReporter(private val failure: () -> Nothing) : QodanaMessageReporter {
    override fun reportError(e: Throwable): Nothing = failure()
    override fun reportError(message: String?): Nothing = failure()
    override fun reportMessage(minVerboseLevel: Int, message: String?) {}
  }

  @Test
  fun `a successful run reports nothing and yields a zero exit code`() {
    assertEquals(0, handle {})

    assertEquals(emptyList<String?>(), reporter.errorMessages)
    assertEquals("", written())
  }

  @Test
  fun `the memory message points the operator at the documentation`() {
    val message = QodanaBundle.message("cli.out.of.memory")

    assertTrue(message, message.contains("jetbrains.com/help/qodana"))
  }

  @Test
  fun `running out of memory is reported as a memory problem, not as a Qodana bug`() {
    assertEquals(1, handle { throw SimulatedHeapExhaustion() })

    // Nothing reaches the reporter: its message would have asked for a bug report.
    assertTrue(reporter.errorMessages.toString(), reporter.errorMessages.isEmpty())
    assertTrue(reporter.errors.toString(), reporter.errors.isEmpty())
    assertEquals(QodanaBundle.message("cli.out.of.memory") + "\n", written())
  }

  @Test
  fun `a failed thread creation is a memory problem, recorded under the native-thread matcher`() {
    // The platform's classifier declines this shape deliberately, so the record must name the hand-written match, or
    // the two routes are indistinguishable in the log. Compared as a whole record, not a substring: SOFT_LIMIT is a
    // prefix of SOFT_LIMIT_FROM_NATIVE_THREAD, so a substring search for one can never rule out the other.
    val warnings = logRecordsFrom(QODANA_LOG_CATEGORY) {
      assertEquals(1, handle {
        throw OutOfMemoryError("unable to create native thread: possibly out of memory or process/resource limits reached")
      })
    }

    val expected = "Memory verdict: " + MemoryVerdict.SOFT_LIMIT_FROM_NATIVE_THREAD.name
    assertTrue(warnings.toString(), warnings.any { it.first == expected })
  }

  @Test
  fun `exhaustion wrapped in a ProcessCanceledException is reported as a memory problem`() {
    // The platform wraps any throwable this way, and reportError(Throwable) would print the whole trace.
    assertEquals(1, handle { throw ProcessCanceledException(SimulatedHeapExhaustion()) })

    assertTrue(reporter.errors.toString(), reporter.errors.isEmpty())
    assertTrue(written(), written().contains("out of memory"))
  }

  @Test
  fun `a recorded abort still wins over exhaustion`() {
    // The reason was authored for the operator, so it renders as itself, with the memory note appended.
    val reason = QodanaCancellationException("analysis timed out")
    reason.addSuppressed(SimulatedHeapExhaustion())

    assertEquals(1, handle { throw ProcessCanceledException(reason) })

    assertTrue(reporter.errors.toString(), reporter.errors.isEmpty())
    val message = reporter.errorMessages.single().orEmpty()
    assertTrue(message, message.contains("analysis timed out"))
    assertTrue(message, message.contains("out of memory"))
    assertEquals("", written())
  }

  @Test
  fun `a recorded abort beside exhaustion is shown without a stack trace`() {
    // DEFAULT.reportError(Throwable) is e.stackTraceToString(), which would print the suppressed exhaustion's whole
    // trace; RecordingMessageReporter stores the throwable unrendered and so cannot catch a regression back to it.
    val reason = QodanaCancellationException("analysis timed out")
    reason.addSuppressed(SimulatedHeapExhaustion())
    val rendering = RenderingMessageReporter()

    assertEquals(1, runBlocking { runReportingTerminalFailure(rendering, sink) { throw ProcessCanceledException(reason) } })

    val shown = rendering.rendered.joinToString("\n")
    assertTrue(shown, shown.contains("analysis timed out"))
    assertFalse(shown, shown.contains(SimulatedHeapExhaustion::class.java.name))
    assertFalse(shown, shown.contains("\tat "))
  }

  @Test
  fun `a limit no container relieves is reported as a bug, under its real name`() {
    // A limit the operator cannot raise stays on the internal-error branch, and the genuine OutOfMemoryError case
    // reaches the operator under its own class name — nothing rewrites it.
    val genuine = OutOfMemoryError("Requested array size exceeds VM limit")
    val excluded = listOf(
      genuine,
      SimulatedHeapExhaustion("Compressed class space"),
      object : VirtualMachineError("CodeCache is full. Compiler has been disabled.") {},
    )

    for (e in excluded) {
      val message = consoleMessage(e, memoryVerdict(e)).orEmpty()

      assertTrue(message, message.contains("internal error"))
      if (e === genuine) assertTrue(message, message.contains("java.lang.OutOfMemoryError"))
    }
  }

  @Test
  fun `an ordinary failure still goes to the reporter`() {
    assertEquals(1, handle { throw RuntimeException("kaboom") })

    assertTrue(reporter.errorMessages.toString(), reporter.errorMessages.single().orEmpty().contains("kaboom"))
    assertEquals("", written())
  }

  @Test
  fun `a failing console does not stop the report`() {
    val broken = object : OutputStream() {
      override fun write(b: Int) = throw IOException("no console")
      override fun write(b: ByteArray) = throw IllegalStateException("not even an IOException")
    }

    // The exit code is the assertion: the caller must still be told to leave. The marker survives the dead console.
    val warnings = logRecordsFrom(QODANA_LOG_CATEGORY) {
      assertEquals(1, runBlocking { runReportingTerminalFailure(reporter, broken) { throw SimulatedHeapExhaustion() } })
    }

    assertTrue(warnings.toString(), warnings.any { "out of memory" in it.first })
  }

  @Test
  fun `a reporter that runs out of memory does not stop the report`() {
    // DEFAULT.reportError(Throwable) is e.stackTraceToString(): it formats a whole trace, and the memory that takes
    // can be exactly what ran out. The caller must still be told to exit.
    val failing = ThrowingMessageReporter { throw SimulatedHeapExhaustion() }

    assertEquals(1, runBlocking {
      runReportingTerminalFailure(failing, sink) {
        throw ProcessCanceledException(QodanaCancellationException("analysis timed out"))
      }
    })
    assertTrue(written(), written().contains("out of memory"))
  }

  @Test
  fun `a reporter that fails for an unrelated reason is not called a memory problem`() {
    // It must not vanish either, so the secondary failure is recorded with its trace, which is safe because it is not
    // the memory error.
    val failing = ThrowingMessageReporter { throw IOException("broken pipe") }

    val warnings = logRecordsFrom(QODANA_LOG_CATEGORY) {
      assertEquals(1, runBlocking { runReportingTerminalFailure(failing, sink) { throw RuntimeException("kaboom") } })
    }

    assertEquals("", written())
    val record = warnings.single { it.first.contains("Could not report") }
    assertEquals("broken pipe", record.second?.message)
  }

  @Test
  fun `the exhaustion route names its matcher and attaches no throwable`() {
    // The canned line alone would leave a misfire of the message match undiagnosable, so the verdict naming the route
    // goes to the log too, in its own record. A throwable on either would format a trace on the dead heap.
    val warnings = logRecordsFrom(QODANA_LOG_CATEGORY) { assertEquals(1, handle { throw SimulatedHeapExhaustion() }) }

    val named = warnings.single { MemoryVerdict.SOFT_LIMIT.name in it.first }
    assertNotEquals(warnings.single { "out of memory" in it.first }, named)
    assertEquals(emptyList<Pair<String, Throwable?>>(), warnings.filter { it.second != null })
    assertFalse(named.first, "\tat " in named.first)
    // The thrown class's name is exactly what this record must not restate.
    assertFalse(named.first, SimulatedHeapExhaustion::class.java.name in named.first)
  }

  @Test
  fun `the exhaustion route never logs the literal TeamCity fails a build for`() {
    // Read from the class rather than typed, so this test's own source cannot plant the string it forbids. A bare
    // OutOfMemoryError is the unsafe shape: its own class name is the trigger, and logRecordsFrom keeps this test's
    // capture of it out of the real build log. This route only — the fixed-ceiling route interpolates the throwable
    // into its message and so does carry the literal, deliberately, and as on master.
    val trigger = OutOfMemoryError::class.java.simpleName
    val warnings = logRecordsFrom(QODANA_LOG_CATEGORY) { assertEquals(1, handle { throw OutOfMemoryError() }) }

    assertTrue(warnings.toString(), warnings.isNotEmpty())
    for ((message, thrown) in warnings) {
      assertFalse(message, trigger in message)
      assertFalse(message, thrown != null && trigger in thrown.toString())
    }
  }

  @Test
  fun `either emission failing still runs the other`() {
    // Old code reported before logging on the cancellation branch, so a throwing reporter there cost the log record.
    val failingReporter = ThrowingMessageReporter { throw IOException("broken pipe") }
    val cancellationWarnings = logRecordsFrom(QODANA_LOG_CATEGORY) {
      assertEquals(1, runBlocking {
        runReportingTerminalFailure(failingReporter, sink) { throw QodanaCancellationException("analysis timed out") }
      })
    }
    assertTrue(cancellationWarnings.toString(), cancellationWarnings.any { "Qodana run cancelled: analysis timed out" in it.first })

    // Old code logged before reporting on the QodanaException branch, so a throwing logger there cost the console
    // message. `Logger.warn` consults `LoggedErrorProcessor` before it formats or emits anything (see
    // `TestLoggerFactory.TestLogger#warn`), so a throwing `processWarn` is a throwing logger with no production change.
    val throwingLogger = object : LoggedErrorProcessor() {
      override fun processWarn(category: String, message: String, t: Throwable?): Boolean {
        if (QODANA_LOG_CATEGORY in category) throw IllegalStateException("no log for you")
        return false
      }
    }
    LoggedErrorProcessor.executeWith<Throwable>(throwingLogger) {
      assertEquals(1, handle { throw QodanaException("boom") })
    }
    val message = reporter.errorMessages.single().orEmpty()
    assertTrue(message, message.contains("Qodana exited abnormally because: boom"))
  }

  @Test
  fun `an argument error with no message forwards null to the reporter`() {
    // A null message means "pass null through", not "emit nothing", so the reporter is called unconditionally. That
    // the operator then reads `null` is a pre-existing wart; pinned here so no `?.let` can swallow the report.
    val warnings = logRecordsFrom(QODANA_LOG_CATEGORY) {
      assertEquals(1, handle { throw InspectionApplicationException(null) })
    }

    assertEquals(listOf<String?>(null), reporter.errorMessages)
    assertEquals(emptyList<Pair<String, Throwable?>>(), warnings)
    assertEquals("", written())
  }

  // Rendering ==========

  private fun logRecordFor(e: Throwable): Pair<String, Throwable?> = logRecord(e, memoryVerdict(e))!!

  @Test
  fun `a limit no container relieves keeps its trace attached to the log`() {
    // Requested array size exceeds VM limit, Compressed class space, and code-cache exhaustion all leave the heap
    // intact, so the trace is exactly what makes the internal-error branch's bug report actionable.
    val fixedCeiling = listOf(
      RuntimeException("failed to write the report", SimulatedHeapExhaustion("Requested array size exceeds VM limit")),
      QodanaException("Failed to wait for the project model", SimulatedHeapExhaustion("Compressed class space")),
      object : VirtualMachineError("CodeCache is full. Compiler has been disabled.") {},
    )

    for (e in fixedCeiling) assertEquals(e.toString(), e, logRecordFor(e).second)
  }

  @Test
  fun `a cancellation carrying exhaustion is logged without a trace`() {
    val reason = QodanaCancellationException("analysis timed out")
    reason.addSuppressed(SimulatedHeapExhaustion())

    val logged = logRecordFor(ProcessCanceledException(reason))

    assertNull(logged.second)
    assertFalse(logged.first, "\tat " in logged.first)
  }
}
