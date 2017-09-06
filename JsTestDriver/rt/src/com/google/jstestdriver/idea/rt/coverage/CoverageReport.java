package com.google.jstestdriver.idea.rt.coverage;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.jstestdriver.idea.rt.util.PathConverter;
import com.intellij.util.containers.PeekableIterator;
import com.intellij.util.containers.PeekableIteratorWrapper;
import org.jetbrains.annotations.NotNull;

import java.io.File;
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
    String normalizedFilePath = PathConverter.getNormalizedPath(new File(filePath));
    normalizeLineHitsList(report);
    List<LineHits> old = myInfo.get(normalizedFilePath);
    if (old != null) {
      doMerge(old, report);
    } else {
      myInfo.put(normalizedFilePath, report);
    }
  }

  public void clearReportByFilePath(@NotNull String filePath) {
    myInfo.remove(filePath);
  }

  private static List<LineHits> doMerge(@NotNull List<LineHits> aList, @NotNull List<LineHits> bList) {
    PeekableIterator<LineHits> ai = new PeekableIteratorWrapper<>(aList.iterator());
    PeekableIterator<LineHits> bi = new PeekableIteratorWrapper<>(bList.iterator());
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

  private static void normalizeLineHitsList(@NotNull List<LineHits> lineHitsList) {
    makeSortedByLineNumber(lineHitsList);
    makeUniqueByLineNumber(lineHitsList);
  }

  private static void makeSortedByLineNumber(@NotNull List<LineHits> report) {
    LineHits prev = null;
    for (LineHits cur : report) {
      if (prev != null && prev.getLineNumber() > cur.getLineNumber()) {
        Collections.sort(report);
        return;
      }
      prev = cur;
    }
  }

  private static void makeUniqueByLineNumber(@NotNull List<LineHits> report) {
    boolean unique = checkForLineUniqueness(report);
    if (unique) {
      return;
    }
    List<LineHits> out = new ArrayList<>(report.size());
    LineHits prev = null;
    for (LineHits cur : report) {
      if (prev != null && prev.getLineNumber() == cur.getLineNumber()) {
        prev.addHits(cur.getHits());
      } else {
        out.add(cur);
        prev = cur;
      }
    }
    report.clear();
    report.addAll(out);
  }

  private static boolean checkForLineUniqueness(@NotNull List<LineHits> lineHitsList) {
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
}
