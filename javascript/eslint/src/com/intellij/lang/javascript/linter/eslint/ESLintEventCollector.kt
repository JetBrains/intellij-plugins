package com.intellij.lang.javascript.linter.eslint

import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector

object ESLintEventCollector : CounterUsagesCollector() {
  private val GROUP = EventLogGroup("js.eslint", 2)
  override fun getGroup(): EventLogGroup = GROUP

  enum class ResponseStatus {
    SUCCESS,
    TIMEOUT
  }

  private val RESPONSE = GROUP.registerEvent(
    "eslint.response",
    EventFields.DurationMs,
    EventFields.Enum<ResponseStatus>("status", "Response status (success/timeout)"))

  fun logResponse(timeMs: Long, status: ResponseStatus) {
    RESPONSE.log(timeMs, status)
  }
}
