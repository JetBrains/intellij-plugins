package org.jetbrains.plugins.cucumber.java.steps.reference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinStepImpl;
import org.jetbrains.plugins.cucumber.steps.reference.CucumberStepReference;

/**
 * User: Andrey.Vokin
 * Date: 7/18/12
 */
public class JavaCucumberStepReferenceProvider extends PsiReferenceProvider {
  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    if (element instanceof GherkinStepImpl) {
      return new PsiReference[] {new CucumberStepReference(element, element.getNode().getTextRange().shiftRight(-element.getTextOffset()))};
    }
    return PsiReference.EMPTY_ARRAY;
  }
}
