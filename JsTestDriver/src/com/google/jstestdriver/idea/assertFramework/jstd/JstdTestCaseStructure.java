package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.jstestdriver.idea.assertFramework.BaseTestCaseStructure;
import com.google.jstestdriver.idea.assertFramework.JsTestFileStructure;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class JstdTestCaseStructure extends BaseTestCaseStructure {

  public JstdTestCaseStructure(@NotNull JsTestFileStructure jsTestFileStructure,
                               @NotNull String testCaseName,
                               @NotNull PsiElement psiElement) {
    super(jsTestFileStructure, testCaseName, psiElement);
  }

}
