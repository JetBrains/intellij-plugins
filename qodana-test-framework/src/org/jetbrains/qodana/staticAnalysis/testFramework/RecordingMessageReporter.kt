// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.qodana.staticAnalysis.testFramework

import org.jetbrains.qodana.util.QodanaMessageReporter

/**
 * A [QodanaMessageReporter] that keeps what it was told, for tests that assert on what the operator would have seen.
 *
 * The three members are kept apart because [QodanaMessageReporter]'s three methods mean different things: a
 * [Throwable] passed to `reportError` is rendered by the reporter itself, a [String] passed to `reportError` was
 * phrased for the operator by the caller, and `reportMessage` is progress rather than an error.
 */
class RecordingMessageReporter : QodanaMessageReporter {
  val errors: MutableList<Throwable> = mutableListOf()
  val errorMessages: MutableList<String?> = mutableListOf()
  val messages: MutableList<String> = mutableListOf()

  override fun reportError(e: Throwable) { errors += e }

  override fun reportError(message: String?) { errorMessages += message }

  override fun reportMessage(minVerboseLevel: Int, message: String?) {
    if (message != null) messages += message
  }
}
