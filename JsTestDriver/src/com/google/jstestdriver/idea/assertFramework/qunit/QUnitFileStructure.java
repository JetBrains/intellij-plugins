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

  private final List<QUnitModuleStructure> myModuleStructures = Lists.newArrayList();
  private final Map<String, QUnitModuleStructure> myModuleStructureByNameMap = Maps.newHashMap();
  private final DefaultQUnitModuleStructure myDefaultModuleStructure = new DefaultQUnitModuleStructure(this);

  public QUnitFileStructure(@NotNull JSFile jsFile) {
    super(jsFile);
  }

  public int getNonDefaultModuleCount() {
    return myModuleStructures.size();
  }

  public List<QUnitModuleStructure> getModuleStructures() {
    return myModuleStructures;
  }

  public void addModuleStructure(@NotNull QUnitModuleStructure moduleStructure) {
    myModuleStructureByNameMap.put(moduleStructure.getName(), moduleStructure);
    myModuleStructures.add(moduleStructure);
  }

  @Nullable
  public QUnitModuleStructure getQUnitModuleByName(String qUnitModuleName) {
    return myModuleStructureByNameMap.get(qUnitModuleName);
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
    for (QUnitModuleStructure moduleStructure : myModuleStructures) {
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
    for (QUnitModuleStructure moduleStructure : myModuleStructures) {
      testMethodStructure = moduleStructure.findTestMethodStructureContainingOffset(offset);
      if (testMethodStructure != null) {
        return testMethodStructure;
      }
    }
    return null;
  }
}
