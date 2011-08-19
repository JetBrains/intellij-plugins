package com.google.jstestdriver.idea.assertFramework.qunit;

import com.google.common.collect.Lists;
import com.google.inject.internal.Maps;
import com.google.jstestdriver.idea.assertFramework.AbstractTestFileStructure;
import com.intellij.lang.javascript.psi.JSFile;
import com.sun.xml.internal.bind.v2.util.QNameMap;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class QUnitFileStructure extends AbstractTestFileStructure {

  private final List<QUnitModuleStructure> myModuleStructures = Lists.newArrayList();
  private Map<String, QUnitModuleStructure> myNameMap = Maps.newHashMap();

  public QUnitFileStructure(@NotNull JSFile jsFile) {
    super(jsFile);
  }

  public int getModuleCount() {
    return myModuleStructures.size();
  }

  public List<QUnitModuleStructure> getModuleStructures() {
    return myModuleStructures;
  }

  public void addModuleStructure(QUnitModuleStructure moduleStructure) {
    myNameMap.put(moduleStructure.getName(), moduleStructure);
    myModuleStructures.add(moduleStructure);
  }

  public QUnitModuleStructure getQUnitModuleByName(String qUnitModuleName) {
    return myNameMap.get(qUnitModuleName);
  }
}
