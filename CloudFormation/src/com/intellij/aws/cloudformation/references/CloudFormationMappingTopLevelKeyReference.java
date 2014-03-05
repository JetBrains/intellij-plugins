package com.intellij.aws.cloudformation.references;

import com.intellij.aws.cloudformation.CloudFormationResolve;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CloudFormationMappingTopLevelKeyReference extends CloudFormationReferenceBase {
  private final String myMappingName;

  public CloudFormationMappingTopLevelKeyReference(@NotNull JSLiteralExpression element, String mappingName) {
    super(element);
    myMappingName = mappingName;
  }

  @Nullable
  @Override
  public PsiElement resolve() {
    final String entityName = CloudFormationResolve.object$.getTargetName(myElement);
    return CloudFormationResolve.object$.resolveTopLevelMappingKey(myElement.getContainingFile(), myMappingName, entityName);
  }

  @NotNull
  public String[] getCompletionVariants() {
    final String[] keys = CloudFormationResolve.object$.getTopLevelMappingKeys(myElement.getContainingFile(), myMappingName);
    return keys == null ? new String[0] : keys;
  }
}
