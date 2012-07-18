package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * User: Andrey.Vokin
 * Date: 7/18/12
 */
public class JavaCucumberStepReferenceProvider extends PsiReferenceProvider {
  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    return new PsiReference[0];  //To change body of implemented methods use File | Settings | File Templates.
  }
}
