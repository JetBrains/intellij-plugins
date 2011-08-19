package com.google.jstestdriver.idea.assertFramework.qunit;

import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Map;

public class MarkedQUnitFileStructure {

  private final Map<Integer, MarkedQUnitModuleStructure> myModules = Maps.newHashMap();

  public MarkedQUnitModuleStructure findById(int id) {
    return myModules.get(id);
  }

  public void addMarkedModuleStructure(MarkedQUnitModuleStructure markedQUnitModuleStructure) {
    if (myModules.containsKey(markedQUnitModuleStructure.getId())) {
      throw new RuntimeException();
    }
    myModules.put(markedQUnitModuleStructure.getId(), markedQUnitModuleStructure);
  }

  public Collection<MarkedQUnitModuleStructure> getModules() {
    return myModules.values();
  }
}
