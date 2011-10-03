package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.jstestdriver.idea.assertFramework.AbstractTestFileStructure;
import com.google.jstestdriver.idea.assertFramework.JstdRunElement;
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
    if (testCaseStructure != null) {
      if (testMethodName != null) {
        JstdTestStructure testStructure = testCaseStructure.getTestStructureByName(testMethodName);
        if (testStructure != null) {
          return testStructure.getTestMethodNameDeclaration();
        }
      } else {
        return testCaseStructure.getEnclosingCallExpression();
      }
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
}
