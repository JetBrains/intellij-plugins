package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.jstestdriver.idea.assertFramework.JstdRunElement;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class JstdTestCaseStructure {

  private final JstdTestFileStructure myJsTestFileStructure;
  private final String myName;
  private final JSCallExpression myEnclosingCallExpression;
  private final List<JstdTestStructure> myTestStructures;
  private final Map<String, JstdTestStructure> myTestStructureByNameMap;
  private final JSObjectLiteralExpression myTestsObjectsLiteral;

  public JstdTestCaseStructure(@NotNull JstdTestFileStructure jsTestFileStructure,
                               @NotNull String name,
                               @NotNull JSCallExpression enclosingCallExpression,
                               @Nullable JSObjectLiteralExpression testsObjectLiteral) {
    myJsTestFileStructure = jsTestFileStructure;
    myName = name;
    myEnclosingCallExpression = enclosingCallExpression;
    myTestsObjectsLiteral = testsObjectLiteral;
    myTestStructures = Lists.newArrayList();
    myTestStructureByNameMap = Maps.newHashMap();
  }

  @NotNull
  public JstdTestFileStructure getJsTestFileStructure() {
    return myJsTestFileStructure;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  public void addTestStructure(@NotNull JstdTestStructure testStructure) {
    myTestStructures.add(testStructure);
    myTestStructureByNameMap.put(testStructure.getName(), testStructure);
  }

  @NotNull
  public JSCallExpression getEnclosingCallExpression() {
    return myEnclosingCallExpression;
  }

  @Nullable
  public JSObjectLiteralExpression getTestsObjectsLiteral() {
    return myTestsObjectsLiteral;
  }

  public JstdTestStructure getTestStructureByName(String testName) {
    return myTestStructureByNameMap.get(testName);
  }

  public int getTestCount() {
    return myTestStructures.size();
  }

  public List<JstdTestStructure> getTestStructures() {
    return myTestStructures;
  }

  @Nullable
  public JstdRunElement findJstdRunElement(@NotNull TextRange textRange) {
    for (JstdTestStructure testStructure : myTestStructures) {
      if (testStructure.containsTextRange(textRange)) {
        return JstdRunElement.newTestMethodRunElement(myName, testStructure.getName());
      }
    }
    TextRange testCaseCallTextRange = myEnclosingCallExpression.getTextRange();
    if (testCaseCallTextRange.contains(textRange)) {
      return JstdRunElement.newTestCaseRunElement(myName);
    }
    return null;
  }
}
