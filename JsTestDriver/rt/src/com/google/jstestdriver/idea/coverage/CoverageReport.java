package com.google.jstestdriver.idea.coverage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Sergey Simonchik
 */
public class CoverageReport {

  private final Map<String, List<LineHits>> myInfo = Maps.newHashMap();

  @NotNull
  public Map<String, List<LineHits>> getInfo() {
    return myInfo;
  }

  public void mergeReport(@NotNull CoverageReport report) {
    for (Map.Entry<String, List<LineHits>> entry : report.getInfo().entrySet()) {
      mergeFileReport(entry.getKey(), entry.getValue());
    }
  }

  public void mergeFileReport(@NotNull String filePath, @NotNull List<LineHits> report) {
    refineReport(report);
    List<LineHits> old = myInfo.get(filePath);
    if (old != null) {
      doMerge(old, report);
    } else {
      myInfo.put(filePath, report);
    }
  }

  public void clearReportByFilePath(String filePath) {
    myInfo.remove(filePath);
  }

  private static List<LineHits> doMerge(@NotNull List<LineHits> aList, @NotNull List<LineHits> bList) {
    PeekingIterator<LineHits> ai = new PeekingIterator<LineHits>(aList.iterator());
    PeekingIterator<LineHits> bi = new PeekingIterator<LineHits>(bList.iterator());
    List<LineHits> out = Lists.newArrayList();
    while (ai.hasNext() && bi.hasNext()) {
      final LineHits x;
      LineHits a = ai.peek();
      LineHits b = bi.peek();
      if (a.getLineNumber() < b.getLineNumber()) {
        x = ai.next();
      }
      else if (a.getLineNumber() > b.getLineNumber()) {
        x = bi.next();
      }
      else {
        a.addHits(b.getHits());
        x = a;
        ai.next();
        bi.next();
      }
      out.add(x);
    }
    addRestItems(out, ai);
    addRestItems(out, bi);
    return out;
  }

  private static <T> void addRestItems(@NotNull List<T> out, Iterator<T> iterator) {
    while (iterator.hasNext()) {
      out.add(iterator.next());
    }
  }

  private static ImmutableList<LineHits> refineReport(@NotNull List<LineHits> report) {
    ImmutableList<LineHits> original = ImmutableList.copyOf(report);
    ImmutableList<LineHits> sorted = makeSortedByLineNumber(original);
    return makeUniqueByLineNumber(sorted);
  }

  private static ImmutableList<LineHits> makeSortedByLineNumber(@NotNull ImmutableList<LineHits> report) {
    LineHits prev = null;
    for (LineHits cur : report) {
      if (prev != null && prev.getLineNumber() > cur.getLineNumber()) {
        LineHits[] array = report.toArray(new LineHits[report.size()]);
        Arrays.sort(array);
        return ImmutableList.of(array);
      }
      prev = cur;
    }
    return report;
  }

  private static ImmutableList<LineHits> makeUniqueByLineNumber(@NotNull ImmutableList<LineHits> report) {
    boolean unique = checkForLineUniqueness(report);
    if (unique) {
      return report;
    }
    List<LineHits> out = Lists.newArrayList();
    LineHits prev = null;
    for (LineHits cur : report) {
      if (prev != null && prev.getLineNumber() == cur.getLineNumber()) {
        prev.addHits(cur.getHits());
      } else {
        out.add(cur);
      }
      prev = cur;
    }
    return ImmutableList.copyOf(out);
  }

  private static boolean checkForLineUniqueness(List<LineHits> lineHitsList) {
    LineHits prev = null;
    for (LineHits cur : lineHitsList) {
      if (prev != null && prev.getLineNumber() == cur.getLineNumber()) {
        return false;
      }
      prev = cur;
    }
    return true;
  }

  public static class LineHits implements Comparable<LineHits> {
    private final int myLineNumber;
    private int myHits;

    public LineHits(int lineNumber, int hits) {
      myLineNumber = lineNumber;
      myHits = hits;
    }

    public int getLineNumber() {
      return myLineNumber;
    }

    public int getHits() {
      return myHits;
    }

    @Override
    public int compareTo(LineHits o) {
      return myLineNumber - o.myLineNumber;
    }

    public void addHits(int hitCount) {
      myHits += hitCount;
    }
  }

  private static class PeekingIterator<T> implements Iterator<T> {

    private final Iterator<T> myIterator;
    private T myValue = null;
    private boolean myValidValue = false;

    private PeekingIterator(Iterator<T> iterator) {
      myIterator = iterator;
      advance();
    }

    @Override
    public boolean hasNext() {
      return myValidValue;
    }

    @Override
    public T next() {
      if (myValidValue) {
        T save = myValue;
        advance();
        return save;
      }
      throw new NoSuchElementException();
    }

    public T peek() {
      if (myValidValue) {
        return myValue;
      }
      throw new NoSuchElementException();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    private void advance() {
      myValidValue = myIterator.hasNext();
      myValue = myValidValue ? myIterator.next() : null;
    }
  }
}
