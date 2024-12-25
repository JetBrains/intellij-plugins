package org.jetbrains.idea.perforce.util.tracer;

import com.intellij.openapi.util.Factory;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

class IntervalStatistics<Data, T extends Consumer<Data>, U extends Consumer<T>> {
  private static final int ourMaxHistoryQueue = 10;
  private static final int ourDefaultInterval = 600 * 1000;

  private final long myIntervalInMillis;
  private final int myHistoryQueueSize;
  private final Factory<? extends T> myFactory;

  private final @NotNull U myAverage;
  private final @NotNull LinkedList<Timed<T>> myRecentComplete = new LinkedList<>();

  private final Object myLock = new Object();

  private @NotNull Timed<T> myCurrent;
  private final @Nullable Runnable mySwitchListener;

  protected IntervalStatistics(long intervalInMillis, int historyQueueSize, final Factory<? extends T> factory, final Factory<? extends U> averageFactory,
                               final @Nullable Runnable switchListener) {
    mySwitchListener = switchListener;
    myIntervalInMillis = (intervalInMillis <= 0) ? ourDefaultInterval : intervalInMillis;
    myHistoryQueueSize = historyQueueSize <= 0 ? ourMaxHistoryQueue : historyQueueSize;
    myFactory = factory;

    myAverage = averageFactory.create();
    myCurrent = new Timed<>(myFactory.create());
  }

  protected void step(final Data data) {
    final long currentTime = System.currentTimeMillis();

    synchronized (myLock) {
      myCurrent.getT().consume(data);
      if ((currentTime - myCurrent.getTime()) < myIntervalInMillis) return;
      
      addToQueue(myCurrent);
      myAverage.consume(myCurrent.getT());
      myCurrent = new Timed<>(myFactory.create());
    }
    if (mySwitchListener != null) {
      mySwitchListener.run();
    }
  }

  private void addToQueue(final Timed<T> timed) {
    if (myRecentComplete.size() == myHistoryQueueSize) {
      myRecentComplete.removeLast();
    }
    myRecentComplete.addFirst(timed);
  }

  public @NotNull U getAverage() {
    return myAverage;
  }

  public List<Timed<T>> receiveRecentComplete() {
    final LinkedList<Timed<T>> result = new LinkedList<>(myRecentComplete);
    myRecentComplete.clear();
    return result;
  }

}
