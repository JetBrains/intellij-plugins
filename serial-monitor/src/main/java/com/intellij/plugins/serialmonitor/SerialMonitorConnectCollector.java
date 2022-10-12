package com.intellij.plugins.serialmonitor;

import com.intellij.internal.statistic.eventLog.EventLogGroup;
import com.intellij.internal.statistic.eventLog.events.EventFields;
import com.intellij.internal.statistic.eventLog.events.EventId2;
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector;
import org.jetbrains.annotations.NotNull;

public class SerialMonitorConnectCollector extends CounterUsagesCollector {

  private static final EventLogGroup GROUP = new EventLogGroup("serial.monitor.connects", 1);

  private static final EventId2<Integer, Boolean> CONNECT_EVENT =
    GROUP.registerEvent(
      "serial.monitor.connected",
      EventFields.Int("baudRate"),
      EventFields.Boolean("success")
    );

  @Override
  @NotNull
  public EventLogGroup getGroup() {
    return GROUP;
  }

  public static void logConnect(int baudRate, boolean success) {
    CONNECT_EVENT.log(baudRate, success);
  }
}
