package com.google.jstestdriver.idea.assertFramework;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class BaseTestCaseStructure {

  private final JsTestFileStructure myJsTestFileStructure;
  private final String myTestCaseName;
  private final PsiElement myPsiElement;
  private final List<BaseTestStructure> myTestStructures;
  private final Map<String, BaseTestStructure> myTestStructureByNameMap;

  public BaseTestCaseStructure(@NotNull JsTestFileStructure jsTestFileStructure,
                               @NotNull String testCaseName,
                               @NotNull PsiElement psiElement) {
    myJsTestFileStructure = jsTestFileStructure;
    myTestCaseName = testCaseName;
    myPsiElement = psiElement;
    myTestStructures = Lists.newArrayList();
    myTestStructureByNameMap = Maps.newHashMap();
  }

  public JsTestFileStructure getJsTestFileStructure() {
    return myJsTestFileStructure;
  }

  public String getTestCaseName() {
    return myTestCaseName;
  }

  public void addTestStructure(BaseTestStructure testStructure) {
    myTestStructures.add(testStructure);
    myTestStructureByNameMap.put(testStructure.getTestName(), testStructure);
  }

  public PsiElement getPsiElement() {
    return myPsiElement;
  }

  public BaseTestStructure getTestStructureByName(String testName) {
    return myTestStructureByNameMap.get(testName);
  }

  public int getTestCount() {
    return myTestStructures.size();
  }

}
