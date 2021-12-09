package org.jetbrains.idea.perforce.util.tracer;

public class TracerParameters {
  private final long myIntervalInMillis;
  private final int myHistoryQueueSize;

  public TracerParameters(long intervalInMillis, int historyQueueSize) {
    myIntervalInMillis = intervalInMillis;
    myHistoryQueueSize = historyQueueSize;
  }

  public long getIntervalInMillis() {
    return myIntervalInMillis;
  }

  public int getHistoryQueueSize() {
    return myHistoryQueueSize;
  }
}
