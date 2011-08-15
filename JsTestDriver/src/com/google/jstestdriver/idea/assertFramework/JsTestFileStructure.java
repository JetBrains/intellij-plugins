package com.google.jstestdriver.idea.assertFramework;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.psi.PsiElement;
import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@NotThreadSafe
public class JsTestFileStructure {

  private final JSFile myJsFile;
  private final List<BaseTestCaseStructure> myTestCaseStructures;
  private final Map<String, BaseTestCaseStructure> myTestCaseStructureByNameMap;

  public JsTestFileStructure(@NotNull JSFile jsFile) {
    myJsFile = jsFile;
    myTestCaseStructures = Lists.newArrayList();
    myTestCaseStructureByNameMap = Maps.newHashMap();
  }

  public List<BaseTestCaseStructure> getTestCaseStructures() {
    return myTestCaseStructures;
  }

  @Nullable
  BaseTestStructure getTestByPsiElement(@NotNull PsiElement psiElement) {
    return null;
  }

  @Nullable
  BaseTestCaseStructure getTestCaseByPsiElement(@NotNull PsiElement psiElement) {
    return null;
  }

  public BaseTestCaseStructure getTestCaseStructureByName(String testCaseName) {
    return myTestCaseStructureByNameMap.get(testCaseName);
  }

  public void addTestCaseStructure(BaseTestCaseStructure testCaseStructure) {
    myTestCaseStructures.add(testCaseStructure);
    myTestCaseStructureByNameMap.put(testCaseStructure.getTestCaseName(), testCaseStructure);
  }

  @NotNull
  public JSFile getJsFile() {
    return myJsFile;
  }

  public int getTestCaseCount() {
    return myTestCaseStructures.size();
  }
}
