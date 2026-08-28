// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.codeInspection.InspectionApplicationException
import com.intellij.diagnostic.DefaultIdeaErrorLogger
import com.intellij.diagnostic.VMOptions.MemoryKind
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.util.ExceptionUtil
import kotlinx.coroutines.CancellationException
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.util.QodanaMessageReporter
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Errors go to stderr, like everything [QodanaMessageReporter.DEFAULT] reports. Written through the descriptor rather
 * than [System.err] to keep the platform's tee into `idea.log`, which allocates, off a path whose memory is gone.
 */
private val STDERR: OutputStream = FileOutputStream(FileDescriptor.err)

/** The last-resort text, and its bytes — encoded up front so the last-resort write has nothing left to build. */
private const val REPORTING_FAILED = "Could not report the failure that terminated the run"
private val REPORTING_FAILED_LINE = (REPORTING_FAILED + "\n").toByteArray()

/**
 * Runs [block], reports whatever terminates it, and returns the process's exit code — zero if it completed.
 *
 * Not every failure arrives here: `licenseCheck`'s license-expired path and
 * `QodanaInspectionApplicationFactory.getApplication` call `exitProcess` themselves.
 *
 * Out-of-memory phrasing is answered from this frame rather than from the formatters below — see
 * [prepareOutOfMemoryReport].
 */
internal suspend fun runReportingTerminalFailure(
  reporter: QodanaMessageReporter,
  sink: OutputStream = STDERR,
  block: suspend () -> Unit,
): Int {
  val logger = Logger.getInstance("#org.jetbrains.qodana.staticAnalysis.terminalFailure")
  val reportOutOfMemory = prepareOutOfMemoryReport(sink, logger)
  // Manually load the classes used on the critical failure path, while there is still heap to load them with.
  // Must be multi-node, or the single-node fast path returns before the walk, and not hard-limit-shaped, or the
  // walk breaks on the first node — a bare RuntimeException cause chain satisfies both, since every node is
  // NOT_APPLICABLE.
  memoryVerdict(RuntimeException(RuntimeException()))
  wasCancelledWithReason(QodanaCancellationException(""))
  try {
    block()
    return 0
  }
  catch (@Suppress("IncorrectCancellationExceptionHandling") e: Throwable) {
    try {
      val memory = memoryVerdict(e)
      // An abort reason authored for the operator describes the run better than the limit that ran out, so it wins.
      if (memory.isSoftLimit && !wasCancelledWithReason(e)) {
        reportOutOfMemory(memory)
      }
      else {
        // `warn`, never `error`: the platform logger rethrows control-flow exceptions, which would skip the exit
        // below. Both emissions run before either is answered for, so neither can cost the other.
        val consoleFailure = failureOf { reporter.reportError(consoleMessage(e, memory)) }
        val logFailure = failureOf { logRecord(e, memory)?.let { (message, thrown) -> logger.warn(message, thrown) } }
        (consoleFailure ?: logFailure)?.let { throw it }
      }
    }
    catch (secondary: Throwable) {
      // Reporting is never the reason the caller skips its exit.
      val memory = memoryVerdict(secondary)
      if (memory.isSoftLimit) {
        reportOutOfMemory(memory)
      }
      else {
        val loggable = if (memory.holdsAnyMemoryError) null else secondary
        ignoringThrowables { logger.warn(REPORTING_FAILED, loggable) }
          ?: ignoringThrowables {
            sink.write(REPORTING_FAILED_LINE)
            sink.flush()
          }
      }
    }
    return 1
  }
}

// Classification ==========

/**
 * What a throwable's cause-and-suppressed graph says about memory, ordered so merging two answers is `maxOf` and the
 * result is always the safest thing to tell the operator: raising a limit only helps if *every* node in the graph
 * agrees it would, so a single [HARD_LIMIT] node outranks any number of soft-limit ones. Telling the operator to
 * raise a limit that will not fix the run is the failure mode this ordering exists to avoid; telling them to report
 * a bug that a limit would in fact have fixed is merely a missed shortcut.
 *
 * An enum rather than a data class: returning a constant allocates nothing, which is all a `catch` on an exhausted
 * heap can afford. Carrying *which* route found the limit lets the log name it without a second walk.
 */
internal enum class MemoryVerdict {
  NOT_APPLICABLE,

  // Reached a memory limit that more available memory relieves
  SOFT_LIMIT,

  // Reached a memory limit that more available memory relieves; found from a native thread spawn exception
  SOFT_LIMIT_FROM_NATIVE_THREAD,

  // Reached a memory limit that more available memory does not relieve
  HARD_LIMIT;

  val isSoftLimit: Boolean get() = this == SOFT_LIMIT || this == SOFT_LIMIT_FROM_NATIVE_THREAD

  /** Gates trace formatting: a trace is built from the whole graph and needs the heap that may already be gone. */
  val holdsAnyMemoryError: Boolean get() = this != NOT_APPLICABLE
}

/**
 * Classifies [e]'s cause-and-suppressed graph, walking it once. Both directions carry memory errors in practice: the
 * platform wraps throwables in a `ProcessCanceledException`, Qodana sites in a [QodanaException], and structured
 * concurrency attaches a sibling coroutine's failure as suppressed.
 *
 * Never throws — callers are `catch` blocks that still owe their caller an exit code. Only [MemoryVerdict.HARD_LIMIT]
 * ends the walk early, because it is the ordering's maximum: no node found later could ever upgrade it. A soft-limit
 * verdict must *not* end the walk, because a [MemoryVerdict.HARD_LIMIT] node elsewhere in the graph would otherwise
 * be missed. Whatever breaks the walk is classified and *merged*, never substituted: the graph's own answer stands.
 */
internal fun memoryVerdict(e: Throwable): MemoryVerdict {
  // Answered before anything allocates, so the shape that arrives in practice needs no memory to be recognised.
  var verdict = nodeVerdict(e)
  // The ordering's maximum: nothing later in the graph could outrank it, so there is nothing left worth walking for.
  if (verdict == MemoryVerdict.HARD_LIMIT) return verdict
  // A single-node graph has nothing for the walk below to find, so this answers without its allocations (a set, a
  // deque, a list and a stream) — the overwhelmingly common shape, a bare `OutOfMemoryError` with no cause or
  // suppressed. `cause` and `suppressed` are both overridable, so this reads them guarded like any other member of a
  // throwable this code does not control.
  if (ignoringThrowables { e.cause == null && e.suppressed.isEmpty() } == true) return verdict
  return try {
    for (node in ExceptionUtil.findCauseAndSuppressed(e, Throwable::class.java)) {
      verdict = maxOf(verdict, nodeVerdict(node))
      if (verdict == MemoryVerdict.HARD_LIMIT) break
    }
    verdict
  }
  catch (t: Throwable) {
    maxOf(verdict, nodeVerdict(t))
  }
}

/**
 * Classifies one node. The two tests are guarded separately because [containerCeilingVerdict] reads `message`, which
 * is overridable and may throw, and one shared guard would discard the type test along with it.
 */
private fun nodeVerdict(t: Throwable): MemoryVerdict {
  ignoringThrowables { containerCeilingVerdict(t) }?.let { return it }
  return if (ignoringThrowables { isMemoryError(t) } == true) MemoryVerdict.HARD_LIMIT else MemoryVerdict.NOT_APPLICABLE
}

/**
 * Whether [t] is a memory error at all — by type, or by the platform's classifier, which is what recognises the
 * `InternalError` carrying code-cache exhaustion. The classifier alone is not enough: it answers null for a
 * thread-creation failure.
 */
private fun isMemoryError(t: Throwable): Boolean =
  t is OutOfMemoryError || DefaultIdeaErrorLogger.getOOMErrorKind(t) != null

/**
 * Which limit a bigger container relieves, or null for none — named by the constant, so the log can say which route
 * answered.
 *
 * Defaults decide, not whether a tuning flag exists: metaspace has no ceiling unless one is set, so the container is
 * its ceiling, while compressed class space carries the JVM's own 1 GB default and the code cache carries every
 * IntelliJ distribution's `-XX:ReservedCodeCacheSize=512m` (VmOptionsGenerator.kt:18).
 *
 * The native-thread shape is admitted by message because [DefaultIdeaErrorLogger.getOOMErrorKind] declines it
 * deliberately — it cannot tell an exhausted memory cap from an exhausted `pids.max`. The classifier stays
 * authoritative elsewhere: it matches the JVM's lower-case `direct buffer memory`, which a hand-written
 * `contains("Direct buffer memory")` would miss.
 */
private fun containerCeilingVerdict(t: Throwable): MemoryVerdict? {
  if (t is OutOfMemoryError) {
    val message = t.message
    if (message != null && message.contains("unable to create") && message.contains("native thread")) {
      return MemoryVerdict.SOFT_LIMIT_FROM_NATIVE_THREAD
    }
  }
  val kind = DefaultIdeaErrorLogger.getOOMErrorKind(t)
  if (kind != MemoryKind.HEAP && kind != MemoryKind.METASPACE && kind != MemoryKind.DIRECT_BUFFERS) return null
  val message = t.message ?: return MemoryVerdict.SOFT_LIMIT
  // "Requested array size exceeds VM limit" is a structure too large to allocate — a Qodana defect, not a limit a
  // bigger container relieves.
  if (message.contains("Requested array size exceeds VM limit") || message.contains("Compressed class space")) return null
  return MemoryVerdict.SOFT_LIMIT
}

/**
 * Whether [e] carries a cancellation reason written for the operator, rather than a bare [ProcessCanceledException],
 * which carries none. Routing only, never a branch selector — [consoleMessage] and [logRecord] select the
 * cancellation branch by type, not by this: a bare [ProcessCanceledException] answers false here yet is still a
 * cancellation.
 */
private fun wasCancelledWithReason(e: Throwable): Boolean = cancellationThrowableToReport(e) is QodanaCancellationException

internal fun cancellationThrowableToReport(throwable: Throwable): Throwable? {
  return when (throwable) {
    is QodanaCancellationException -> throwable
    is ProcessCanceledException -> (throwable.cause as? QodanaCancellationException) ?: throwable
    else -> null
  }
}

// Phrasing ==========

/**
 * What the operator is told about [e], or null to hand the reporter a null — which prints `null`, a pre-existing wart
 * this does not fix.
 */
internal fun consoleMessage(e: Throwable, memory: MemoryVerdict): String? = when (e) {
  // Invalid arguments or a malformed qodana.yaml: a message authored for the operator, contracted to carry no trace.
  is InspectionApplicationException -> e.message
  // QodanaCancellationException, ProcessCanceledException, IndicatorCancellationException — all CancellationException.
  is CancellationException -> {
    // A bare ProcessCanceledException has no message, and rendering it would name its class at the operator.
    val reason = (cancellationThrowableToReport(e) ?: e).message ?: QodanaBundle.message("cli.run.cancelled")
    if (memory.isSoftLimit) reason + "\n" + QodanaBundle.message("cli.out.of.memory") else reason
  }
  is QodanaException -> "Qodana exited abnormally because: ${e.message}"
  // The trace is a developer diagnostic and goes to idea.log only; the console gets one actionable line.
  else -> QodanaBundle.message("cli.internal.error", e.toString(), PathManager.getLogDir().resolve("idea.log"))
}

/**
 * The `idea.log` record for [e], or null to log nothing — which only an [InspectionApplicationException] asks for.
 *
 * The throwable is withheld only when [memory] is a soft limit: that is the shape where the heap genuinely may be
 * gone, and formatting a trace from the whole graph needs the heap that may already be gone. A
 * [MemoryVerdict.HARD_LIMIT] leaves the heap intact — the array was too large, or the code cache or compressed
 * class space ran out — so its trace is exactly what makes the internal-error branch's bug report actionable, and it
 * is attached. The [Pair] allocates, and that is accepted: every branch that returns one builds a message anyway, and
 * the soft-limit route never gets here.
 */
internal fun logRecord(e: Throwable, memory: MemoryVerdict): Pair<String, Throwable?>? {
  val loggable = if (memory.isSoftLimit) null else e
  return when (e) {
    is InspectionApplicationException -> null
    is CancellationException -> {
      val toReport = cancellationThrowableToReport(e) ?: e
      val reason = toReport.message ?: QodanaBundle.message("cli.run.cancelled")
      // The trace goes as text, never as the throwable: Logger.ensureNotControlFlow swaps a control-flow throwable
      // for a synthetic one.
      ("Qodana run cancelled: $reason" + loggable?.let { "\n" + toReport.stackTraceToString() }.orEmpty()) to null
    }
    is QodanaException -> "Qodana exited abnormally" to loggable
    else -> "Qodana failed with a terminal error: $e" to loggable
  }
}

// Emission ==========

/**
 * Encodes the operator's line now and returns what writes it later, so the failure path has nothing left to build —
 * one syscall and two log calls.
 *
 * [QodanaBundle.message] wraps a missing key in `!`s rather than throwing, so `null` here means the bundle itself was
 * unresolvable; the caller then gets a no-op rather than a key recited at the operator.
 */
private fun prepareOutOfMemoryReport(sink: OutputStream, logger: Logger): (MemoryVerdict) -> Unit {
  val text = ignoringThrowables { QodanaBundle.message("cli.out.of.memory") }
  if (text == null) {
    ignoringThrowables { logger.warn("Could not prepare the out-of-memory report") }
    return {}
  }
  val line = (text + "\n").toByteArray()
  return { verdict ->
    ignoringThrowables {
      sink.write(line)
      sink.flush()
    }
    // A marker for whoever reads `idea.log` once the console output is gone, then the route that classified this, so
    // a misfire of the newer message match is diagnosable. Neither carries a throwable — that would format a trace on
    // the dead heap — and `verdict.name` is read inside its guard, where a failure cannot escape and skip the exit.
    ignoringThrowables { logger.warn(text) }
    ignoringThrowables { logger.warn("Memory verdict: " + verdict.name) }
  }
}

/** Not `runCatching`: its [Result.Failure] allocation can itself fail on the heap this exists to survive. */
private inline fun <T> ignoringThrowables(action: () -> T): T? =
  try {
    action()
  }
  catch (@Suppress("unused") t: Throwable) {
    null
  }

/** Whatever [action] threw, so the caller can run the next emission before answering for it. Allocates nothing. */
private inline fun failureOf(action: () -> Unit): Throwable? =
  try {
    action()
    null
  }
  catch (t: Throwable) {
    t
  }
