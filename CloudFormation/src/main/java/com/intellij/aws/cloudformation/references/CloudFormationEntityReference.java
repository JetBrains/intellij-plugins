package com.intellij.aws.cloudformation.references;

import com.intellij.aws.cloudformation.CloudFormationMetadataProvider;
import com.intellij.aws.cloudformation.CloudFormationResolve;
import com.intellij.aws.cloudformation.CloudFormationSections;
import com.intellij.json.psi.JsonLiteral;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

public class CloudFormationEntityReference extends CloudFormationReferenceBase {
  private final Collection<String> myPossibleSections;

  @Nullable
  private final Collection<String> myExcludeFromVariants;

  public CloudFormationEntityReference(@NotNull JsonLiteral element,
                                       @NotNull Collection<String> possibleSections,
                                       @Nullable Collection<String> variantsToExclude) {
    super(element);
    myExcludeFromVariants = variantsToExclude;

    assert possibleSections.size() > 0;
    myPossibleSections = possibleSections;
  }

  @Nullable
  @Override
  public PsiElement resolve() {
    final String entityName = StringUtil.stripQuotesAroundValue(StringUtil.notNullize(myElement.getText()));
    return CloudFormationResolve.Companion.resolveEntity(myElement.getContainingFile(), entityName, myPossibleSections);
  }

  @NotNull
  public String[] getCompletionVariants() {
    Set<String> entities = CloudFormationResolve.Companion.getEntities(myElement.getContainingFile(), myPossibleSections);

    if (myExcludeFromVariants != null) {
      entities.removeAll(myExcludeFromVariants);
    }

    if (myPossibleSections.contains(CloudFormationSections.Parameters)) {
      entities.addAll(CloudFormationMetadataProvider.METADATA.predefinedParameters);
    }

    return ArrayUtil.toStringArray(entities);
  }
}
