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
}
