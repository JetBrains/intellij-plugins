package com.google.jstestdriver.idea.assertFramework.qunit;

import com.google.common.collect.Lists;
import com.google.inject.internal.Maps;
import com.google.jstestdriver.idea.assertFramework.AbstractTestFileStructure;
import com.google.jstestdriver.idea.util.JsPsiUtils;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class QUnitFileStructure extends AbstractTestFileStructure {

  private final List<QUnitModuleStructure> myNonDefaultModuleStructures = Lists.newArrayList();
  private final Map<String, QUnitModuleStructure> myModuleStructureByNameMap = Maps.newHashMap();
  private final DefaultQUnitModuleStructure myDefaultModuleStructure = new DefaultQUnitModuleStructure(this);

  public QUnitFileStructure(@NotNull JSFile jsFile) {
    super(jsFile);
  }

  public int getAllModuleCount() {
    return myNonDefaultModuleStructures.size() + 1;
  }

  public int getNonDefaultModuleCount() {
    return myNonDefaultModuleStructures.size();
  }

  public List<QUnitModuleStructure> getNonDefaultModuleStructures() {
    return myNonDefaultModuleStructures;
  }

  public void addModuleStructure(@NotNull QUnitModuleStructure moduleStructure) {
    myModuleStructureByNameMap.put(moduleStructure.getName(), moduleStructure);
    myNonDefaultModuleStructures.add(moduleStructure);
  }

  @Nullable
  public AbstractQUnitModuleStructure getQUnitModuleByName(String qUnitModuleName) {
    AbstractQUnitModuleStructure moduleStructure = myModuleStructureByNameMap.get(qUnitModuleName);
    if (moduleStructure == null) {
      if (myDefaultModuleStructure.getName().equals(qUnitModuleName)) {
        moduleStructure = myDefaultModuleStructure;
      }
    }
    return moduleStructure;
  }

  @NotNull
  public DefaultQUnitModuleStructure getDefaultModuleStructure() {
    return myDefaultModuleStructure;
  }

  public boolean hasQUnitSymbols() {
    return getDefaultModuleStructure().getTestCount() > 0 || getNonDefaultModuleCount() > 0;
  }

  @Nullable
  public QUnitModuleStructure findModuleStructureContainingOffset(int offset) {
    for (QUnitModuleStructure moduleStructure : myNonDefaultModuleStructures) {
      TextRange moduleTextRange = moduleStructure.getEnclosingCallExpression().getTextRange();
      if (JsPsiUtils.containsOffsetStrictly(moduleTextRange, offset)) {
        return moduleStructure;
      }
    }
    return null;
  }

  @Nullable
  public QUnitTestMethodStructure findTestMethodStructureContainingOffset(int offset) {
    QUnitTestMethodStructure testMethodStructure = myDefaultModuleStructure.findTestMethodStructureContainingOffset(offset);
    if (testMethodStructure != null) {
      return testMethodStructure;
    }
    for (QUnitModuleStructure moduleStructure : myNonDefaultModuleStructures) {
      testMethodStructure = moduleStructure.findTestMethodStructureContainingOffset(offset);
      if (testMethodStructure != null) {
        return testMethodStructure;
      }
    }
    return null;
  }
}
