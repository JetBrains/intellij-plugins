package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.javascript.testFramework.AbstractTestFileStructure;
import com.intellij.javascript.testFramework.AbstractTestStructureElement;
import com.intellij.javascript.testFramework.JstdRunElement;
import com.intellij.javascript.testFramework.util.JsPsiUtils;
import com.intellij.lang.javascript.psi.JSDefinitionExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class JstdTestFileStructure extends AbstractTestFileStructure {

  private final List<JstdTestCaseStructure> myTestCaseStructures;
  private final Map<String, JstdTestCaseStructure> myTestCaseStructureByNameMap;
  private Map<PsiElement, String> myNameByPsiElementMap;
  private Map<PsiElement, Void> myPrototypeBasedTestElements;

  public JstdTestFileStructure(@NotNull JSFile jsFile) {
    super(jsFile);
    myTestCaseStructures = Lists.newArrayList();
    myTestCaseStructureByNameMap = Maps.newHashMap();
  }

  @Override
  public boolean isEmpty() {
    for (JstdTestCaseStructure testCaseStructure : myTestCaseStructures) {
      if (testCaseStructure.getTestCount() > 0) {
        return false;
      }
    }
    return true;
  }

  public List<JstdTestCaseStructure> getTestCaseStructures() {
    return myTestCaseStructures;
  }

  public JstdTestCaseStructure getTestCaseStructureByName(String testCaseName) {
    return myTestCaseStructureByNameMap.get(testCaseName);
  }

  public void addTestCaseStructure(JstdTestCaseStructure testCaseStructure) {
    myTestCaseStructures.add(testCaseStructure);
    myTestCaseStructureByNameMap.put(testCaseStructure.getName(), testCaseStructure);
  }

  public int getTestCaseCount() {
    return myTestCaseStructures.size();
  }

  @Nullable
  public String getNameByPsiElement(@NotNull PsiElement element) {
    return myNameByPsiElementMap.get(element);
  }

  public boolean isPrototypeTestElement(@NotNull PsiElement element) {
    return myPrototypeBasedTestElements.containsKey(element);
  }

  @Override
  @Nullable
  public JstdRunElement findJstdRunElement(@NotNull TextRange textRange) {
    for (JstdTestCaseStructure testCaseStructure : myTestCaseStructures) {
      JstdRunElement jstdRunElement = testCaseStructure.findJstdRunElement(textRange);
      if (jstdRunElement != null) {
        return jstdRunElement;
      }
    }
    return null;
  }

  @Override
  public PsiElement findPsiElement(@NotNull String testCaseName, @Nullable String testMethodName) {
    JstdTestCaseStructure testCaseStructure = myTestCaseStructureByNameMap.get(testCaseName);
    if (testCaseStructure == null) {
      return null;
    }
    if (testMethodName == null) {
      return testCaseStructure.getEnclosingCallExpression();
    }
    JstdTestStructure testStructure = testCaseStructure.getTestStructureByName(testMethodName);
    if (testStructure != null) {
      return testStructure.getTestMethodNameDeclaration();
    }
    return null;
  }

  @Nullable
  public JstdTestCaseStructure findEnclosingTestCaseByOffset(int documentOffset) {
    for (JstdTestCaseStructure testCaseStructure : myTestCaseStructures) {
      TextRange testCaseCallExpressionTextRange = testCaseStructure.getEnclosingCallExpression().getTextRange();
      if (JsPsiUtils.containsOffsetStrictly(testCaseCallExpressionTextRange, documentOffset)) {
        return testCaseStructure;
      }
    }
    return null;
  }

  @NotNull
  @Override
  public List<String> getTopLevelElements() {
    if (myTestCaseStructures.isEmpty()) {
      return Collections.emptyList();
    }
    List<String> out = new ArrayList<>(myTestCaseStructures.size());
    for (JstdTestCaseStructure structure : myTestCaseStructures) {
      out.add(structure.getName());
    }
    return out;
  }

  @NotNull
  @Override
  public List<String> getChildrenOf(@NotNull String topLevelElementName) {
    JstdTestCaseStructure testCaseStructure = myTestCaseStructureByNameMap.get(topLevelElementName);
    if (testCaseStructure == null) {
      return Collections.emptyList();
    }
    List<String> out = new ArrayList<>(testCaseStructure.getTestCount());
    for (JstdTestStructure testStructure : testCaseStructure.getTestStructures()) {
      out.add(testStructure.getName());
    }
    return out;
  }

  @Override
  public boolean contains(@NotNull String testCaseName, @Nullable String testMethodName) {
    return findPsiElement(testCaseName, testMethodName) != null;
  }

  @Override
  public List<? extends AbstractTestStructureElement> getChildren() {
    return myTestCaseStructures;
  }

  void postProcess() {
    myNameByPsiElementMap = Collections.emptyMap();
    myPrototypeBasedTestElements = Collections.emptyMap();
    if (myTestCaseStructures.isEmpty()) {
      return;
    }
    int totalCount = 0;
    int prototypeBasedTestCount = 0;
    for (JstdTestCaseStructure testCaseStructure : myTestCaseStructures) {
      totalCount += testCaseStructure.getTestCount() + 1;
      for (JstdTestStructure testStructure : testCaseStructure.getTestStructures()) {
        if (testStructure.getWholeLeftDefExpr() != null) {
          prototypeBasedTestCount++;
        }
      }
    }
    myNameByPsiElementMap = new IdentityHashMap<>(totalCount);
    if (prototypeBasedTestCount > 0) {
      myPrototypeBasedTestElements = new IdentityHashMap<>(prototypeBasedTestCount);
    }
    for (JstdTestCaseStructure testCaseStructure : myTestCaseStructures) {
      JSExpression testCaseMethodExpr = testCaseStructure.getEnclosingCallExpression().getMethodExpression();
      if (testCaseMethodExpr != null) {
        myNameByPsiElementMap.put(testCaseMethodExpr, testCaseStructure.getName());
      }
      for (JstdTestStructure testStructure : testCaseStructure.getTestStructures()) {
        PsiElement anchor = testStructure.getTestMethodNameDeclaration();
        myNameByPsiElementMap.put(anchor, testStructure.getName());
        JSDefinitionExpression wholeLeftDefExpr = testStructure.getWholeLeftDefExpr();
        if (wholeLeftDefExpr != null) {
          myPrototypeBasedTestElements.put(wholeLeftDefExpr, null);
        }
      }
    }
  }

}
