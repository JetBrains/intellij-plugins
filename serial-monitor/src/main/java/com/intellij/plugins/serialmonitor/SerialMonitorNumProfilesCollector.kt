package com.intellij.plugins.serialmonitor

import com.intellij.internal.statistic.beans.MetricEvent
import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.events.EventFields.Int
import com.intellij.internal.statistic.service.fus.collectors.ApplicationUsagesCollector
import com.intellij.openapi.components.service

internal class SerialMonitorNumProfilesCollector : ApplicationUsagesCollector() {
  override fun getGroup(): EventLogGroup = GROUP

  override fun getMetrics(): Set<MetricEvent> {
    val profileService: SerialProfileService = service<SerialProfileService>()
    return setOf(NUMBER_SAVED_PROFILES.metric(
      profileService.getProfiles().size,
      profileService.defaultBaudRate()
    ))
  }

  private val GROUP = EventLogGroup("serial.monitor.profiles", 1)
  private val NUMBER_SAVED_PROFILES = GROUP.registerEvent("serial.profiles",
                                                          Int("saved"),
                                                          Int("defaultBaudrate"))
}
