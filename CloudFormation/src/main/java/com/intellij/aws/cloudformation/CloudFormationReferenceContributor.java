package com.intellij.aws.cloudformation;

import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import org.jetbrains.annotations.NotNull;

public class CloudFormationReferenceContributor extends PsiReferenceContributor {
  private CloudFormationReferenceProvider ReferenceProviderInstance = new CloudFormationReferenceProvider();

  @Override
  public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(PlatformPatterns.psiElement(JsonStringLiteral.class), ReferenceProviderInstance);
  }
}
