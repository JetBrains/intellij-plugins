// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.coverage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DartCoverageData {
  private @Nullable List<DartFileCoverageData> coverage;

  public @Nullable List<DartFileCoverageData> getCoverage() {
    return coverage;
  }

  public void setCoverage(@Nullable List<DartFileCoverageData> coverage) {
    this.coverage = coverage;
  }

  public @NotNull Map<String, SortedMap<Integer, Integer>> getMergedDartFileCoverageData() {
    Map<String, SortedMap<Integer, Integer>> mergedCoverageData = new HashMap<>();
    List<DartFileCoverageData> coverageData = getCoverage();
    if (coverageData != null) {
      for (DartFileCoverageData item : coverageData) {
        String source = item.getSource();
        if (source == null) {
          continue;
        }
        SortedMap<Integer, Integer> fileData = mergedCoverageData.computeIfAbsent(source, k -> new TreeMap<>());
        List<Integer> hits = item.getHits();
        if (hits == null) {
          continue;
        }
        for (int i = 0; i < hits.size(); i += 2) {
          Integer lineNumber = hits.get(i);
          Integer hitCount = hits.get(i + 1);
          if (!fileData.containsKey(lineNumber)) {
            fileData.put(lineNumber, 0);
          }

          fileData.put(lineNumber, fileData.get(lineNumber) + hitCount);
        }
      }
    }

    return mergedCoverageData;
  }
}


class DartFileCoverageData {
  private @Nullable String source;
  private @Nullable List<Integer> hits;

  public @Nullable String getSource() {
    return source;
  }

  public void setSource(@Nullable String source) {
    this.source = source;
  }

  public @Nullable List<Integer> getHits() {
    return hits;
  }

  public void setHits(@Nullable List<Integer> hits) {
    this.hits = hits;
  }
}