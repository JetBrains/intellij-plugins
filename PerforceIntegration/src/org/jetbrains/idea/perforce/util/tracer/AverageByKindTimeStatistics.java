package org.jetbrains.idea.perforce.util.tracer;

import com.intellij.openapi.util.Pair;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class AverageByKindTimeStatistics<Kind extends Enum> extends
      IntervalStatistics<Pair<Kind, Long>, AverageByKindTimeStatistics.Data<Kind>, AverageByKindTimeStatistics.Average<Kind>> implements Tracer<Kind, Long> {

  // todo do not pass switch listener
  AverageByKindTimeStatistics(long intervalInMillis, int historyQueueSize, @Nullable Runnable switchListener) {
    super(intervalInMillis, historyQueueSize, () -> new Data<>(), () -> new Average<>(), switchListener);
  }

  @Override
  public Long start(final Kind kind, final String presentation) {
    return System.currentTimeMillis();
  }

  @Override
  public void stop(final @NotNull Long key, final Kind kind, final String presentation) {
    step(new Pair<>(kind, System.currentTimeMillis() - key));
  }

  public static class Data<Kind> implements Consumer<Pair<Kind, Long>> {
    private final Map<Kind, Pair<Long, Integer>> myMap;

    public Data() {
      myMap = new HashMap<>();
    }

    @Override
    public void consume(Pair<Kind, Long> kindLongPair) {
      final Kind kind = kindLongPair.getFirst();
      final Pair<Long, Integer> current = myMap.get(kind);
      if (current == null) {
        myMap.put(kind, new Pair<>(kindLongPair.getSecond(), 1));
      } else {
        myMap.put(kind, new Pair<>(current.getFirst() + kindLongPair.getSecond(), current.getSecond() + 1));
      }
    }

    public Map<Kind, Pair<Long, Integer>> getMap() {
      return myMap;
    }
  }

  public static class Average<Kind> implements Consumer<Data<Kind>> {
    private final Map<Kind, Pair<BigDecimal, Long>> myMap;

    public Average() {
      myMap = new HashMap<>();
    }

    @Override
    public void consume(Data<Kind> kindData) {
      final Map<Kind, Pair<Long, Integer>> comingMap = kindData.getMap();
      for (Kind kind : comingMap.keySet()) {
        final Pair<BigDecimal, Long> existing = myMap.get(kind);
        final Pair<Long, Integer> comingValue = comingMap.get(kind);
        final BigDecimal valueToAdd = new BigDecimal(comingValue.getFirst());

        if (existing == null) {
          myMap.put(kind, new Pair<>(valueToAdd, comingValue.getSecond().longValue()));
        } else {
          myMap.put(kind, new Pair<>(existing.getFirst().add(valueToAdd), existing.getSecond() + comingValue.getSecond()));
        }
      }
    }

    public Map<Kind, Pair<BigDecimal, Long>> getMap() {
      return Collections.unmodifiableMap(myMap);
    }
  }
}
