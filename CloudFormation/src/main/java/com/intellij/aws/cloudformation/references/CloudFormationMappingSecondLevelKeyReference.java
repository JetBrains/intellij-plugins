package com.intellij.aws.cloudformation.references;

import com.intellij.aws.cloudformation.CloudFormationResolve;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CloudFormationMappingSecondLevelKeyReference extends CloudFormationReferenceBase {
  private final String myMappingName;
  private final String myTopLevelKey;

  public CloudFormationMappingSecondLevelKeyReference(@NotNull JsonStringLiteral element, String mappingName, String topLevelKey) {
    super(element);
    myMappingName = mappingName;
    myTopLevelKey = topLevelKey;
  }

  @Nullable
  @Override
  public PsiElement resolve() {
    final String entityName = CloudFormationResolve.OBJECT$.getTargetName((JsonStringLiteral) myElement);
    return CloudFormationResolve.OBJECT$.resolveSecondLevelMappingKey(myElement.getContainingFile(), myMappingName, myTopLevelKey, entityName);
  }

  @NotNull
  public String[] getCompletionVariants() {
    final String[] keys = CloudFormationResolve.OBJECT$.getSecondLevelMappingKeys(myElement.getContainingFile(), myMappingName, myTopLevelKey);
    return keys == null ? new String[0] : keys;
  }
}
