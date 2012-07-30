package com.google.jstestdriver.idea.assertFramework;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class TestFileStructurePack {

  private final List<AbstractTestFileStructure> myTestFileStructures;

  public TestFileStructurePack(List<AbstractTestFileStructure> testFileStructures) {
    myTestFileStructures = testFileStructures;
  }

  public boolean isEmpty() {
    for (AbstractTestFileStructure structure : myTestFileStructures) {
      if (!structure.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  @Nullable
  public JstdRunElement getJstdRunElement(@NotNull PsiElement psiElement) {
    TextRange textRange = psiElement.getTextRange();
    for (AbstractTestFileStructure testFileStructure : myTestFileStructures) {
      JstdRunElement jstdRunElement = testFileStructure.findJstdRunElement(textRange);
      if (jstdRunElement != null) {
        return jstdRunElement;
      }
    }
    return null;
  }

  @Nullable
  public PsiElement findPsiElement(@NotNull String testCaseName, @Nullable String testMethodName) {
    for (AbstractTestFileStructure testFileStructure : myTestFileStructures) {
      PsiElement element = testFileStructure.findPsiElement(testCaseName, testMethodName);
      if (element != null) {
        return element;
      }
    }
    return null;
  }

  @NotNull
  public List<AbstractTestFileStructure> getTestFileStructures() {
    return myTestFileStructures;
  }
}
