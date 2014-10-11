package com.intellij.aws.cloudformation;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;

public class CloudFormationReferenceContributor extends PsiReferenceContributor {
  private CloudFormationReferenceProvider ReferenceProviderInstance = new CloudFormationReferenceProvider();

  @Override
  public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(PlatformPatterns.psiElement(JSLiteralExpression.class), ReferenceProviderInstance);
  }
}
