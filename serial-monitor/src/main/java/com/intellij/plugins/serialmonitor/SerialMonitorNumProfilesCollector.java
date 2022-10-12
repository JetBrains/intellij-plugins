package com.intellij.plugins.serialmonitor;

import com.intellij.internal.statistic.beans.MetricEvent;
import com.intellij.internal.statistic.eventLog.EventLogGroup;
import com.intellij.internal.statistic.eventLog.events.EventFields;
import com.intellij.internal.statistic.eventLog.events.EventId2;
import com.intellij.internal.statistic.service.fus.collectors.ApplicationUsagesCollector;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

public class SerialMonitorNumProfilesCollector extends ApplicationUsagesCollector {

  private static final EventLogGroup GROUP = new EventLogGroup("serial.monitor.profiles", 1);
  private static final EventId2<Integer, Integer> NUMBER_SAVED_PROFILES =
    GROUP.registerEvent("serial.profiles",
                        EventFields.Int("saved"),
                        EventFields.Int("defaultBaudrate"));

  @Override
  @NotNull
  public EventLogGroup getGroup() {
    return GROUP;
  }

  @NotNull
  @Override
  public Set<MetricEvent> getMetrics() {
    SerialProfileService profileService = SerialProfileService.getInstance();
    return Collections.singleton(NUMBER_SAVED_PROFILES.metric(
      profileService.getProfiles().size(),
      profileService.defaultBaudRate()
      ));
  }
}
