package com.google.jstestdriver.idea.assertFramework.qunit;

import com.google.common.collect.Lists;
import com.google.inject.internal.Maps;
import com.google.jstestdriver.idea.assertFramework.JstdRunElement;
import com.google.jstestdriver.idea.util.JsPsiUtils;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class AbstractQUnitModuleStructure {

  private final QUnitFileStructure myFileStructure;
  private final String myName;
  private final List<QUnitTestMethodStructure> myTestMethodStructures = Lists.newArrayList();
  private final Map<String, QUnitTestMethodStructure> myTestMethodStructureByNameMap = Maps.newHashMap();

  public AbstractQUnitModuleStructure(@NotNull QUnitFileStructure fileStructure, @NotNull String name) {
    myFileStructure = fileStructure;
    myName = name;
  }

  @NotNull
  public QUnitFileStructure getFileStructure() {
    return myFileStructure;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  public void addTestMethodStructure(QUnitTestMethodStructure qUnitTestMethodStructure) {
    myTestMethodStructures.add(qUnitTestMethodStructure);
    myTestMethodStructureByNameMap.put(qUnitTestMethodStructure.getName(), qUnitTestMethodStructure);
  }

  @Nullable
  public QUnitTestMethodStructure getTestMethodStructureByName(String testMethodName) {
    return myTestMethodStructureByNameMap.get(testMethodName);
  }

  @NotNull
  public List<QUnitTestMethodStructure> getTestMethodStructures() {
    return myTestMethodStructures;
  }

  public boolean isDefault() {
    return this instanceof DefaultQUnitModuleStructure;
  }

  public int getTestCount() {
    return myTestMethodStructures.size();
  }

  @Nullable
  public QUnitTestMethodStructure findTestMethodStructureContainingOffset(int offset) {
    for (QUnitTestMethodStructure testMethodStructure : myTestMethodStructures) {
      TextRange testTextRange = testMethodStructure.getCallExpression().getTextRange();
      if (JsPsiUtils.containsOffsetStrictly(testTextRange, offset)) {
        return testMethodStructure;
      }
    }
    return null;
  }

  @Nullable
  JstdRunElement findJstdRunElement(@NotNull TextRange textRange) {
    for (QUnitTestMethodStructure testMethodStructure : myTestMethodStructures) {
      TextRange testTextRange = testMethodStructure.getCallExpression().getTextRange();
      if (testTextRange.contains(textRange)) {
        return JstdRunElement.newTestMethodRunElement(myName, "test " + testMethodStructure.getName());
      }
    }
    return null;
  }
}
