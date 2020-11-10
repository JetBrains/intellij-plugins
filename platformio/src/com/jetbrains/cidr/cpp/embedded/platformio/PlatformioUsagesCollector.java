package com.jetbrains.cidr.cpp.embedded.platformio;

import com.intellij.internal.statistic.eventLog.EventLogGroup;
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector;
import com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioActionBase;

public class PlatformioUsagesCollector extends CounterUsagesCollector {
  @Override
  public EventLogGroup getGroup() {
    return PlatformioActionBase.EVENT_LOG_GROUP;
  }
}
