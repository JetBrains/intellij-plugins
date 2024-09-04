package com.intellij.plugins.serialmonitor

import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector

internal object SerialMonitorCollector : CounterUsagesCollector() {

  val GROUP = EventLogGroup("serial.monitor", 1)

  val CONNECT_EVENT =
    GROUP.registerEvent(
      "serial.monitor.connected",
      EventFields.Int("baudRate"),
      EventFields.Boolean("success")
    )

  val SAVE_EVENT = GROUP.registerEvent("serial.monitor.log.saved", EventFields.RoundedInt("lines"))

  override fun getGroup() = GROUP

  fun logConnect(baudRate: Int, success: Boolean) {
    CONNECT_EVENT.log(baudRate, success)
  }

  fun logSave(lines: Int) {
    SAVE_EVENT.log(lines)
  }
}
