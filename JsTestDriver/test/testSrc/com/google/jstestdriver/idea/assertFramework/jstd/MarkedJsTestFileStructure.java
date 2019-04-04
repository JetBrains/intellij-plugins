package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

class MarkedJsTestFileStructure {

  private final List<MarkedTestCaseStructure> myMarkedTestCaseStructures = Lists.newArrayList();
  private final Map<Integer, MarkedTestCaseStructure> myMarkedTestCaseStructureByIdMap = Maps.newHashMap();

  public void addMarkedTestCaseStructure(MarkedTestCaseStructure markedTestCaseStructure) {
    myMarkedTestCaseStructures.add(markedTestCaseStructure);
    myMarkedTestCaseStructureByIdMap.put(markedTestCaseStructure.getId(), markedTestCaseStructure);
  }

  public MarkedTestCaseStructure findById(int testCaseId) {
    return myMarkedTestCaseStructureByIdMap.get(testCaseId);
  }

  public List<MarkedTestCaseStructure> getMarkedTestCaseStructures() {
    return myMarkedTestCaseStructures;
  }
}
