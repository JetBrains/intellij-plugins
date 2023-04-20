package org.jetbrains.idea.perforce.util.tracer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Time;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

class LongCallsPresentation<Kind extends Enum> implements StatisticsPresentation<LongCallsStatistics<Kind>> {
  @Override
  public void putSelf(final StringBuilder sb, LongCallsStatistics<Kind> statistics) {
    final LongCallsStatistics.AverageData<Kind> averageData = statistics.getAverage();
    final SortedMap<Long, LongCallsStatistics.Data<Kind>> averageMap = averageData.getMap();
    sb.append("Long calls\n")
      .append("Average:\n");
    putMap(sb, averageMap);

    sb.append("Recent calls:\n");
    final List<Timed<LongCallsStatistics.IntervalData<Kind>>> list = statistics.receiveRecentComplete();
    for (Timed<LongCallsStatistics.IntervalData<Kind>> timed : list) {
      sb.append("Statistics at ").append(new Time(timed.getTime()));
      putMap(sb, timed.getT().getMap());
    }
  }

  private void putMap(final StringBuilder sb, final SortedMap<Long, LongCallsStatistics.Data<Kind>> map) {
    for (Map.Entry<Long, LongCallsStatistics.Data<Kind>> entry : map.entrySet()) {
      final LongCallsStatistics.Data<Kind> data = entry.getValue();
      sb.append("execution time ")
        .append(entry.getKey())
        .append(" ms\nfor: '")
        .append(data.getPresentation())
        .append("'\nat: ");

      final Throwable throwable = data.getStackTraceHolder();
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      final PrintStream stream = new PrintStream(baos);
      throwable.printStackTrace(stream);

      sb.append(baos).append('\n');
    }
  }
}
