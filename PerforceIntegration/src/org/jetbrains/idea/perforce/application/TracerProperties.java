package org.jetbrains.idea.perforce.application;

import java.util.Properties;

public enum TracerProperties {
  averageTimesInterval("perforce.tracer.average.times.interval", 3600 * 1000),
  averageTimesQueueSize("perforce.tracer.average.times.queue.size", 1),

  numberConcurrentThreadsInterval("perforce.tracer.number.concurrent.threads.interval", 10000),
  numberConcurrentThreadsQueueSize("perforce.tracer.number.concurrent.threads.queue.size", 10),

  longCallsInterval("perforce.tracer.long.calls.interval", 10000),
  longCallsQueueSize("perforce.tracer.long.calls.queue.size", 10),
  longCallsMaxKept("perforce.tracer.long.max.kept", 10),
  longCallsLowerBound("perforce.tracer.long.calls.lower.bound", 5000),

  outputInterval("perforce.tracer.log.interval", 3 * 600 * 1000);

  public static final String GATHER_AVERAGE_TIMES = "perforce.tracer.average.gather";
  public static final String GATHER_CONCURRENT_THREADS = "perforce.tracer.number.concurrent.threads.gather";
  public static final String GATHER_LONG_CALLS = "perforce.tracer.long.calls.gather";

  private final String myName;
  private final long myDefault;

  TracerProperties(String name, long aDefault) {
    myName = name;
    myDefault = aDefault;
  }

  public long getDefault() {
    return myDefault;
  }

  public long getValue(final Properties properties) {
    final String stringValue = properties.getProperty(myName);
    final Long value = stringValue == null ? null : Long.valueOf(stringValue);
    return (value == null) ? myDefault : (value <= 0 ? myDefault : value);
  }
}
