package com.google.jstestdriver.idea.assertFramework.jasmine;

import com.google.common.collect.Maps;
import com.google.jstestdriver.idea.assertFramework.CompoundId;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

class MarkedJasmineSuiteStructureContainer {

  private final Map<CompoundId, MarkedJasmineSuiteStructure> myInnerSuiteByIdMap = Maps.newHashMap();

  public void addSuiteStructure(@NotNull MarkedJasmineSuiteStructure markedJasmineSuiteStructure) {
    myInnerSuiteByIdMap.put(markedJasmineSuiteStructure.getId(), markedJasmineSuiteStructure);
  }

  public MarkedJasmineSuiteStructure findSuiteStructureById(CompoundId id) {
    return myInnerSuiteByIdMap.get(id);
  }

  public Collection<MarkedJasmineSuiteStructure> getInnerSuiteStructures() {
    return myInnerSuiteByIdMap.values();
  }
}
