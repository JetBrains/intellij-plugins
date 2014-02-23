package com.intellij.aws.cloudformation.references;

import com.intellij.aws.cloudformation.CloudFormationResolve;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CloudFormationEntityReference extends CloudFormationReferenceBase {
  private final String[] myPossibleSections;

  public CloudFormationEntityReference(@NotNull JSLiteralExpression element, String... possibleSections) {
    super(element);

    assert possibleSections.length > 0;
    myPossibleSections = possibleSections;
  }

  @Nullable
  @Override
  public PsiElement resolve() {
    final String entityName = StringUtil.stripQuotesAroundValue(StringUtil.notNullize(myElement.getText()));
    return CloudFormationResolve.resolveEntity(myElement.getContainingFile(), entityName, myPossibleSections);
  }

  @NotNull
  public String[] getCompletionVariants() {
    return CloudFormationResolve.getEntities(myElement.getContainingFile(), myPossibleSections);
  }
}
