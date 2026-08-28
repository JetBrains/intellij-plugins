// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.qodana.staticAnalysis.testFramework

import com.intellij.testFramework.LoggedErrorProcessor
import java.util.concurrent.CopyOnWriteArrayList

/** Matches every category Qodana's own loggers use, whether derived from a class or named explicitly. */
const val QODANA_LOG_CATEGORY: String = "org.jetbrains.qodana"

/**
 * Collects what [block] logged at warn or error level through a category containing [categoryPrefix], and keeps those
 * records out of the build log.
 *
 * [LoggedErrorProcessor] is installed process-wide for the duration of [block], so any thread that logs lands here;
 * the filter and the concurrent list are both about that. Match with `contains` rather than a prefix comparison:
 * `Logger.getInstance` prepends `#` to a class-derived category, and the processor receives that raw name.
 *
 * Suppressing is the point of the return values, but only for records that match [categoryPrefix]: those are kept out
 * of the build log, and out of the failure that an unhandled `Action.ALL` error would otherwise cause. Everything
 * else falls through to the platform default, so unrelated warnings and errors still surface normally.
 */
fun logRecordsFrom(categoryPrefix: String, block: () -> Unit): List<Pair<String, Throwable?>> {
  val records = CopyOnWriteArrayList<Pair<String, Throwable?>>()
  val collector = object : LoggedErrorProcessor() {
    override fun processWarn(category: String, message: String, t: Throwable?): Boolean {
      if (categoryPrefix !in category) return true
      records += message to t
      return false
    }

    override fun processError(category: String, message: String, details: Array<String>, t: Throwable?): Set<Action> {
      if (categoryPrefix !in category) return Action.ALL
      records += message to t
      return Action.NONE
    }
  }
  LoggedErrorProcessor.executeWith<Throwable>(collector) { block() }
  return records
}
