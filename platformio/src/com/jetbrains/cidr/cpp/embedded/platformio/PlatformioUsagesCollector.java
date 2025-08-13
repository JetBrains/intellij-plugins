package com.jetbrains.cidr.cpp.embedded.platformio;

import com.intellij.internal.statistic.eventLog.EventLogGroup;
import com.intellij.internal.statistic.eventLog.events.EventId;
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector;

public final class PlatformioUsagesCollector extends CounterUsagesCollector {

  public static final EventLogGroup EVENT_LOG_GROUP = new EventLogGroup("cidr.embedded.platformio", 3);

  public static final EventId DEBUG_START_EVENT_ID = EVENT_LOG_GROUP.registerEvent("start-debug");

  public static final EventId NEW_PROJECT = EVENT_LOG_GROUP.registerEvent("new-project");

  @Override
  public EventLogGroup getGroup() {
    return EVENT_LOG_GROUP;
  }
}
