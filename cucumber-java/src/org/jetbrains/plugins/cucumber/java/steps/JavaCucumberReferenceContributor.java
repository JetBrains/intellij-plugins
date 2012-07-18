package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;

/**
 * User: Andrey.Vokin
 * Date: 7/18/12
 */
public class JavaCucumberReferenceContributor extends PsiReferenceContributor {
  @Override
  public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(PlatformPatterns.psiElement(PsiMethod.class), new JavaCucumberStepReferenceProvider());
  }
}
