package com.jetbrains.cidr.cpp.embedded.platformio;

import com.intellij.internal.statistic.eventLog.EventLogGroup;
import com.intellij.internal.statistic.eventLog.events.EventId;
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector;

public class PlatformioUsagesCollector extends CounterUsagesCollector {

  public static final EventLogGroup EVENT_LOG_GROUP = new EventLogGroup("cidr.embedded.platformio", 2);

  public static final EventId DEBUG_START_EVENT_ID = EVENT_LOG_GROUP.registerEvent("start-debug");

  public static final EventId PROJECT_OPEN_VIA_HOME_ID = EVENT_LOG_GROUP.registerEvent("project-open-via-home");
  public static final EventId NEW_PROJECT = EVENT_LOG_GROUP.registerEvent("new-project");

  public static final EventId FILE_OPEN_VIA_HOME_ID = EVENT_LOG_GROUP.registerEvent("file-open-via-home");

  @Override
  public EventLogGroup getGroup() {
    return EVENT_LOG_GROUP;
  }
}
