package org.jetbrains.plugins.cucumber.java.steps.reference;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinStepImpl;

/**
 * User: Andrey.Vokin
 * Date: 7/18/12
 */
public class JavaCucumberReferenceContributor extends PsiReferenceContributor {
  @Override
  public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(PlatformPatterns.psiElement(GherkinStepImpl.class),
                                        new JavaCucumberStepReferenceProvider());
  }
}
