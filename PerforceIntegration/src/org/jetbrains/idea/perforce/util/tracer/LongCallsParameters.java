package org.jetbrains.idea.perforce.util.tracer;

public class LongCallsParameters extends TracerParameters {
  private final int myMaxKept;
  private final long myLowerBound;

  public LongCallsParameters(long intervalInMillis, int historyQueueSize, int maxKept, long lowerBound) {
    super(intervalInMillis, historyQueueSize);
    myMaxKept = maxKept;
    myLowerBound = lowerBound;
  }

  public int getMaxKept() {
    return myMaxKept;
  }

  public long getLowerBound() {
    return myLowerBound;
  }
}
