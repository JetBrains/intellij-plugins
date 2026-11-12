package com.jetbrains.cidr.cpp.embedded.platformio

import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector

internal object PlatformioUsagesCollector : CounterUsagesCollector() {
  override fun getGroup(): EventLogGroup {
    return EVENT_LOG_GROUP
  }

  private val EVENT_LOG_GROUP = EventLogGroup("cidr.embedded.platformio", 3)

  val DEBUG_START_EVENT_ID = EVENT_LOG_GROUP.registerEvent("start-debug")

  val NEW_PROJECT = EVENT_LOG_GROUP.registerEvent("new-project")
}
