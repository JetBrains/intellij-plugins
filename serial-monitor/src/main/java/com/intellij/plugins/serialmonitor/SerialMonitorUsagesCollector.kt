package com.intellij.plugins.serialmonitor

import com.intellij.internal.statistic.beans.MetricEvent
import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.events.EventFields.Int
import com.intellij.internal.statistic.service.fus.collectors.ApplicationUsagesCollector
import com.intellij.openapi.application.readActionBlocking
import com.intellij.openapi.components.service
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.plugins.serialmonitor.service.PortStatus
import com.intellij.plugins.serialmonitor.service.SerialPortService
import com.intellij.plugins.serialmonitor.ui.SERIAL_MONITOR

internal class SerialMonitorUsagesCollector : ApplicationUsagesCollector() {
  override fun getGroup(): EventLogGroup = GROUP

  private suspend fun gatherTabMetric(): MetricEvent {
    var total = 0
    var timestamped = 0
    var hex = 0
    var connected = 0
    ProjectManager.getInstance().openProjects
      .filter { !it.isDisposed && !it.isDefault }
      .mapNotNull { project -> ToolWindowManager.getInstance(project).getToolWindow("Serial Monitor")?.contentManagerIfCreated }
      .flatMap { contentManager ->
        readActionBlocking {
          contentManager.contents
        }.mapNotNull { content ->
          content.getUserData(SERIAL_MONITOR)
        }
      }
      .forEach { serialMonitor ->
        total++
        if (serialMonitor.isTimestamped()) {
          timestamped++
        }
        if (serialMonitor.isHex()) {
          hex++
        }
        if (serialMonitor.getStatus() == PortStatus.CONNECTED) {
          connected++
        }
      }

    return TAB_COUNTS.metric(
      TABS_TOTAL.with(total),
      TABS_TIMESTAMPED.with(timestamped),
      TABS_HEX.with(hex),
      TABS_CONNECTED.with(connected)
    )
  }

  override suspend fun getMetricsAsync(): Set<MetricEvent> {
    val profileService: SerialProfileService = service<SerialProfileService>()
    val portService: SerialPortService = service<SerialPortService>()
    val tabMetric = gatherTabMetric()

    return mutableSetOf(
      NUMBER_SAVED_PROFILES.metric(
        profileService.getProfiles().size,
        profileService.defaultBaudRate()
      ),
      AVAILABLE_PORTS.metric(
        portService.getPortsNames().size
      ),
      tabMetric
    )
  }

  private val GROUP = EventLogGroup("serial.monitor.usages", 1)
  private val NUMBER_SAVED_PROFILES = GROUP.registerEvent("serial.monitor.usages.profiles",
                                                          Int("saved"),
                                                          Int("defaultBaudrate"))
  private val AVAILABLE_PORTS = GROUP.registerEvent("serial.monitor.usages.ports", Int("count"))

  private val TABS_TOTAL = Int("total")
  private val TABS_TIMESTAMPED = Int("timestamped")
  private val TABS_HEX = Int("hex")
  private val TABS_CONNECTED = Int("connected")
  private val TAB_COUNTS = GROUP.registerVarargEvent("serial.monitor.usages.tabs", TABS_TOTAL, TABS_TIMESTAMPED, TABS_HEX, TABS_CONNECTED)
}
