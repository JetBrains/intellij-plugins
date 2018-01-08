package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.BDDFrameworkType;

public class CucumberStepDefinitionCreationContext {
  private PsiFile myPsiFile;

  @Nullable
  private BDDFrameworkType myBDDFrameworkType;

  public CucumberStepDefinitionCreationContext() {
  }

  public CucumberStepDefinitionCreationContext(@Nullable PsiFile psiFile, @Nullable BDDFrameworkType BDDFrameworkType) {
    myPsiFile = psiFile;
    myBDDFrameworkType = BDDFrameworkType;
  }

  public PsiFile getPsiFile() {
    return myPsiFile;
  }

  public void setPsiFile(@Nullable PsiFile psiFile) {
    myPsiFile = psiFile;
  }

  @Nullable
  public BDDFrameworkType getBDDFrameworkType() {
    return myBDDFrameworkType;
  }

  public void setBDDFrameworkType(@Nullable BDDFrameworkType BDDFrameworkType) {
    myBDDFrameworkType = BDDFrameworkType;
  }
}
