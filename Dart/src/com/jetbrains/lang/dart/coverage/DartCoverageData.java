/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.lang.dart.coverage;

import com.intellij.util.containers.hash.HashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class DartCoverageData {
  @Nullable private List<DartFileCoverageData> coverage;

  @Nullable
  public List<DartFileCoverageData> getCoverage() {
    return coverage;
  }

  public void setCoverage(@Nullable List<DartFileCoverageData> coverage) {
    this.coverage = coverage;
  }

  @NotNull
  public Map<String, SortedMap<Integer, Integer>> getMergedDartFileCoverageData() {
    Map<String, SortedMap<Integer, Integer>> mergedCoverageData = new HashMap<>();
    List<DartFileCoverageData> coverageData = getCoverage();
    if (coverageData != null) {
      for (DartFileCoverageData item : coverageData) {
        String source = item.getSource();
        if (source == null) {
          continue;
        }
        if (!mergedCoverageData.containsKey(source)) {
          mergedCoverageData.put(source, new TreeMap<>());
        }

        SortedMap<Integer, Integer> fileData = mergedCoverageData.get(source);
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
  @Nullable private String source;
  @Nullable private List<Integer> hits;

  @Nullable
  public String getSource() {
    return source;
  }

  public void setSource(@Nullable String source) {
    this.source = source;
  }

  @Nullable
  public List<Integer> getHits() {
    return hits;
  }

  public void setHits(@Nullable List<Integer> hits) {
    this.hits = hits;
  }
}