package org.jetbrains.idea.perforce.util.tracer;

import com.intellij.util.Consumer;
import org.jetbrains.annotations.Nullable;

import java.util.SortedMap;
import java.util.TreeMap;

class LongCallsStatistics<Kind extends Enum>
  extends IntervalStatistics<LongCallsStatistics.Data<Kind>, LongCallsStatistics.IntervalData<Kind>, LongCallsStatistics.AverageData<Kind>> implements Tracer<Kind, Long> {

  private static final int ourDefaultMaxKept = 20;
  private static final long ourDefaultLowerBound = 1000;
  private final long myLowerBound;

  LongCallsStatistics(long intervalInMillis, int historyQueueSize, @Nullable Runnable switchListener, final int maxKept, long lowerBound) {
    super(intervalInMillis, historyQueueSize, () -> new IntervalData<>(maxKept <= 0 ? ourDefaultMaxKept : maxKept), () -> new AverageData<>(
      maxKept <= 0 ? ourDefaultMaxKept : maxKept), switchListener);
    myLowerBound = lowerBound < 0 ? ourDefaultLowerBound : lowerBound;
  }

  @Override
  public Long start(Kind kind, String presentation) {
    return System.currentTimeMillis();
  }

  @Override
  public void stop(final Long key, final Kind kind, final String presentation) {
    final long interval = System.currentTimeMillis() - key;
    if (interval < myLowerBound) return;

    step(new Data<>(presentation, interval));
  }

  static class MyMapHolder<Kind> {
    private final int myMaxKept;
    private final SortedMap<Long, Data<Kind>> myMap;

    MyMapHolder(int maxKept) {
      myMaxKept = maxKept;
      myMap = new TreeMap<>();
    }

    public void accept(final Data<Kind> kindData) {
      final long interval = kindData.getInterval();

      if ((! myMap.isEmpty()) && (myMap.firstKey() > interval)) return;
      if (myMap.size() == myMaxKept) {
        myMap.remove(myMap.firstKey());
      }
      myMap.put(interval, kindData);
    }

    public SortedMap<Long, Data<Kind>> getMap() {
      return myMap;
    }
  }

  public static class IntervalData<Kind> extends MyMapHolder<Kind> implements Consumer<Data<Kind>> {
    public IntervalData(final int maxKept) {
      super(maxKept);
    }

    @Override
    public void consume(final Data<Kind> kindData) {
      accept(kindData);
    }
  }

  public static class AverageData<Kind> extends MyMapHolder<Kind> implements Consumer<IntervalData<Kind>> {
    public AverageData(int maxKept) {
      super(maxKept);
    }

    @Override
    public void consume(final IntervalData<Kind> kindIntervalData) {
      final SortedMap<Long, Data<Kind>> map = kindIntervalData.getMap();
      for (Data<Kind> kindData : map.values()) {
        accept(kindData);
      }
    }
  }

  public static class Data<Kind> {
    // todo +-
    private final Throwable myStackTraceHolder;
    private final String myPresentation;
    private final long myInterval;

    public Data(final String presentation, long interval) {
      myPresentation = presentation;
      myInterval = interval;
      myStackTraceHolder = new Throwable();
    }

    public Throwable getStackTraceHolder() {
      return myStackTraceHolder;
    }

    public String getPresentation() {
      return myPresentation;
    }

    public long getInterval() {
      return myInterval;
    }
  }
}
