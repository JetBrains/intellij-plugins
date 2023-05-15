package org.jetbrains.idea.perforce.util.tracer;

import com.intellij.openapi.util.Factory;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

class IntervalStatistics<Data, T extends Consumer<Data>, U extends Consumer<T>> {
  private final static int ourMaxHistoryQueue = 10;
  private final static int ourDefaultInterval = 600 * 1000;

  private final long myIntervalInMillis;
  private final int myHistoryQueueSize;
  private final Factory<? extends T> myFactory;

  @NotNull
  private final U myAverage;
  @NotNull
  private final LinkedList<Timed<T>> myRecentComplete = new LinkedList<>();

  private final Object myLock = new Object();

  @NotNull
  private Timed<T> myCurrent;
  @Nullable
  private final Runnable mySwitchListener;

  protected IntervalStatistics(long intervalInMillis, int historyQueueSize, final Factory<? extends T> factory, final Factory<? extends U> averageFactory,
                               @Nullable final Runnable switchListener) {
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

  @NotNull
  public U getAverage() {
    return myAverage;
  }

  public List<Timed<T>> receiveRecentComplete() {
    final LinkedList<Timed<T>> result = new LinkedList<>(myRecentComplete);
    myRecentComplete.clear();
    return result;
  }

}
