package org.jetbrains.idea.perforce.util.tracer;

import com.intellij.util.Consumer;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

class ConcurrentThreadsStatistics<Kind extends Enum>
  extends IntervalStatistics<Boolean, ConcurrentThreadsStatistics.MaxCurrentPair, ConcurrentThreadsStatistics.MaxAveragePairAverage>
  implements Tracer<Kind, Object> {

  ConcurrentThreadsStatistics(long intervalInMillis, int historyQueueSize, @Nullable Runnable switchListener) {
    super(intervalInMillis, historyQueueSize, () -> new MaxCurrentPair(), () -> new MaxAveragePairAverage(), switchListener);
  }

  @Override
  public Object start(final Kind kind, final String presentation) {
    step(true);
    return this;
  }

  @Override
  public void stop(Object o, Kind kind, String presentation) {
    step(false);
  }

  public static class MaxCurrentPair implements Consumer<Boolean> {
    private int myCurrent;
    private int myMax;

    public MaxCurrentPair() {
      myCurrent = 0;
      myMax = 0;
    }

    @Override
    public void consume(final Boolean entered) {
      if (entered) {
        enter();
      } else {
        exit();
      }
    }

    public void enter() {
      ++ myCurrent;
      if (myCurrent > myMax) {
        myMax = myCurrent;
      }
    }

    public void exit() {
      -- myCurrent;
    }

    public int getMax() {
      return myMax;
    }
  }

  public static class MaxAveragePairAverage implements Consumer<MaxCurrentPair> {
    private int myMax;
    private BigDecimal myTotal;
    private int myCnt;

    public MaxAveragePairAverage() {
      myMax = 0;
      myTotal = new BigDecimal(0);
      myCnt = 0;
    }

    @Override
    public void consume(final MaxCurrentPair pair) {
      final int max = pair.getMax();
      if (max > myMax) {
        myMax = max;
      }
      ++ myCnt;
      myTotal = myTotal.add(new BigDecimal(max));
    }

    public int getMax() {
      return myMax;
    }

    public BigDecimal getTotal() {
      return myTotal;
    }

    public int getCnt() {
      return myCnt;
    }
  }

  // remember only maximum number inside a tick
  /*public static class MaxAveragePairInTick implements Consumer<MaxCurrentPair> {
    private int myMax;
    private long myTotal;
    private int myCnt;

    public MaxAveragePairInTick() {
      myMax = 0;
      myTotal = 0;
      myCnt = 0;
    }

    public void consume(final MaxCurrentPair pair) {
      final int max = pair.getMax();
      if (max > myMax) {
        myMax = max;
      }
      ++ myCnt;
      myTotal += max;
    }

    public int getMax() {
      return myMax;
    }

    public long getTotal() {
      return myTotal;
    }

    public int getCnt() {
      return myCnt;
    }
  }*/
}
