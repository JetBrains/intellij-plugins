package com.intellij.aws.cloudformation;

import com.intellij.aws.cloudformation.references.CloudFormationEntityReference;
import com.intellij.aws.cloudformation.references.CloudFormationMappingTopLevelKeyReference;
import com.intellij.lang.javascript.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CloudFormationReferenceProvider extends PsiReferenceProvider {
  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    if (!CloudFormationPsiUtils.isCloudFormationFile(element)) {
      return PsiReference.EMPTY_ARRAY;
    }

    if (element instanceof JSLiteralExpression) {
      final PsiReference reference = buildFromElement(element);
      if (reference != null) {
        return new PsiReference[]{reference};
      }
    }

    return PsiReference.EMPTY_ARRAY;
  }

  @Nullable
  public static PsiReference buildFromElement(PsiElement element) {
    final JSLiteralExpression literalExpression = ObjectUtils.tryCast(element, JSLiteralExpression.class);
    if (literalExpression == null) {
      return null;
    }

    if (isInRef(literalExpression)) {
      return new CloudFormationEntityReference(
        literalExpression,
        CloudFormationSections.Parameters,
        CloudFormationSections.Resources);
    }

    // Fn::GetAtt && Fn::FindInMap
    final JSArrayLiteralExpression parametersArray = ObjectUtils.tryCast(element.getParent(), JSArrayLiteralExpression.class);
    if (parametersArray != null) {
      final JSProperty funcProperty = ObjectUtils.tryCast(parametersArray.getParent(), JSProperty.class);
      final boolean isFindInMap = funcProperty != null && CloudFormationIntrinsicFunctions.FnFindInMap.equals(funcProperty.getName());
      final boolean isGetAtt = funcProperty != null && CloudFormationIntrinsicFunctions.FnGetAtt.equals(funcProperty.getName());

      if (isGetAtt || isFindInMap) {
        final JSObjectLiteralExpression obj = ObjectUtils.tryCast(funcProperty.getParent(), JSObjectLiteralExpression.class);
        if (obj != null) {
          final JSExpression[] allParameters = parametersArray.getExpressions();
          if (allParameters.length > 0 && element == allParameters[0]) {
            final JSProperty[] properties = obj.getProperties();
            if (properties.length == 1) {
              if (isGetAtt) {
                return new CloudFormationEntityReference(literalExpression, CloudFormationSections.Resources);
              }

              return new CloudFormationEntityReference(literalExpression, CloudFormationSections.Mappings);
            }
          } else if (allParameters.length > 1 && element == allParameters[1]) {
            if (isFindInMap) {
              //
              JSLiteralExpression mappingNameExpression = ObjectUtils.tryCast(allParameters[0], JSLiteralExpression.class);
              if (mappingNameExpression != null && mappingNameExpression.isQuotedLiteral()) {
                return new CloudFormationMappingTopLevelKeyReference(literalExpression, CloudFormationResolve.getTargetName(mappingNameExpression));
              }
            }
          }
        }
      }
    }

    if (isInDependsOnSingle(element)) {
      return new CloudFormationEntityReference(
        literalExpression,
        CloudFormationSections.Resources);
    }

    if (isInDependsOnMultiple(element)) {
      return new CloudFormationEntityReference(
        literalExpression,
        CloudFormationSections.Resources);
    }

    return null;
  }

  public static boolean isInRef(JSLiteralExpression element) {
    final JSProperty refProperty = ObjectUtils.tryCast(element.getParent(), JSProperty.class);
    if (refProperty == null || !CloudFormationIntrinsicFunctions.Ref.equals(refProperty.getName())) {
      return false;
    }

    final JSObjectLiteralExpression obj = ObjectUtils.tryCast(refProperty.getParent(), JSObjectLiteralExpression.class);
    if (obj == null) {
      return false;
    }

    final JSProperty[] properties = obj.getProperties();
    if (properties.length != 1) {
      return false;
    }

    final String targetName = CloudFormationResolve.getTargetName(element);
    return !CloudFormationMetadataProvider.METADATA.predefinedParameters.contains(targetName);
  }

  public static boolean isInDependsOnSingle(PsiElement element) {
    final JSProperty dependsOnProperty = ObjectUtils.tryCast(element.getParent(), JSProperty.class);
    if (dependsOnProperty == null || !CloudFormationConstants.DependsOn.equals(dependsOnProperty.getName())) {
      return false;
    }

    final JSObjectLiteralExpression resourceProperties =
      ObjectUtils.tryCast(dependsOnProperty.getParent(), JSObjectLiteralExpression.class);
    if (resourceProperties == null) {
      return false;
    }

    final JSProperty resource = ObjectUtils.tryCast(resourceProperties.getParent(), JSProperty.class);
    if (resource == null) {
      return false;
    }

    final PsiElement entity = CloudFormationResolve.resolveEntity(
      element.getContainingFile(), resource.getName(),
      CloudFormationSections.Resources);

    return resource == entity;
  }

  public static boolean isInDependsOnMultiple(PsiElement element) {
    final JSArrayLiteralExpression refArray = ObjectUtils.tryCast(element.getParent(), JSArrayLiteralExpression.class);
    if (refArray == null) {
      return false;
    }

    final JSProperty dependsOnProperty = ObjectUtils.tryCast(refArray.getParent(), JSProperty.class);
    if (dependsOnProperty == null || !CloudFormationConstants.DependsOn.equals(dependsOnProperty.getName())) {
      return false;
    }

    final JSObjectLiteralExpression resourceProperties =
      ObjectUtils.tryCast(dependsOnProperty.getParent(), JSObjectLiteralExpression.class);
    if (resourceProperties == null) {
      return false;
    }

    final JSProperty resource = ObjectUtils.tryCast(resourceProperties.getParent(), JSProperty.class);
    if (resource == null) {
      return false;
    }

    final PsiElement entity = CloudFormationResolve.resolveEntity(
      element.getContainingFile(), resource.getName(),
      CloudFormationSections.Resources);

    return resource == entity;
  }
}
