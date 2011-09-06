package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.jstestdriver.idea.assertFramework.AbstractTestFileStructure;
import com.google.jstestdriver.idea.util.JsPsiUtils;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@NotThreadSafe
public class JstdTestFileStructure extends AbstractTestFileStructure {

  private final List<JstdTestCaseStructure> myTestCaseStructures;
  private final Map<String, JstdTestCaseStructure> myTestCaseStructureByNameMap;

  public JstdTestFileStructure(@NotNull JSFile jsFile) {
    super(jsFile);
    myTestCaseStructures = Lists.newArrayList();
    myTestCaseStructureByNameMap = Maps.newHashMap();
  }

  public List<JstdTestCaseStructure> getTestCaseStructures() {
    return myTestCaseStructures;
  }

  @Nullable
  JstdTestStructure getTestByPsiElement(@NotNull PsiElement psiElement) {
    return null;
  }

  @Nullable
  JstdTestCaseStructure getTestCaseByPsiElement(@NotNull PsiElement psiElement) {
    return null;
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
  public JstdTestCaseStructure findEnclosingTestCaseByOffset(int documentOffset) {
    for (JstdTestCaseStructure testCaseStructure : myTestCaseStructures) {
      TextRange testCaseCallExpressionTextRange = testCaseStructure.getEnclosingCallExpression().getTextRange();
      if (JsPsiUtils.containsOffsetStrictly(testCaseCallExpressionTextRange, documentOffset)) {
        return testCaseStructure;
      }
    }
    return null;
  }
}
