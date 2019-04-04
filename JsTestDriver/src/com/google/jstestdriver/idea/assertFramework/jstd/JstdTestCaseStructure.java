package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.javascript.testFramework.AbstractTestStructureElement;
import com.intellij.javascript.testFramework.JstdRunElement;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class JstdTestCaseStructure extends AbstractTestStructureElement<JstdTestCaseStructure> {

  private final JstdTestFileStructure myJsTestFileStructure;
  private final List<JstdTestStructure> myTestStructures;
  private final Map<String, JstdTestStructure> myTestStructureByNameMap;
  private final JSObjectLiteralExpression myTestsObjectsLiteral;

  public JstdTestCaseStructure(@NotNull JstdTestFileStructure jsTestFileStructure,
                               @NotNull String name,
                               @NotNull JSCallExpression enclosingCallExpression,
                               @Nullable JSObjectLiteralExpression testsObjectLiteral) {
    super(enclosingCallExpression, name, null);
    myJsTestFileStructure = jsTestFileStructure;
    myTestsObjectsLiteral = testsObjectLiteral;
    myTestStructures = Lists.newArrayList();
    myTestStructureByNameMap = Maps.newHashMap();
  }

  @NotNull
  public JstdTestFileStructure getJsTestFileStructure() {
    return myJsTestFileStructure;
  }

  public void addTestStructure(@NotNull JstdTestStructure testStructure) {
    myTestStructures.add(testStructure);
    myTestStructureByNameMap.put(testStructure.getName(), testStructure);
  }

  @NotNull
  public JSCallExpression getEnclosingCallExpression() {
    return (JSCallExpression)getEnclosingPsiElement();
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
    TextRange testCaseCallTextRange = getEnclosingCallExpression().getTextRange();
    if (testCaseCallTextRange.contains(textRange)) {
      return JstdRunElement.newTestCaseRunElement(myName);
    }
    return null;
  }
}
