package com.intellij.aws.cloudformation;

import com.intellij.aws.cloudformation.references.CloudFormationEntityReference;
import com.intellij.aws.cloudformation.references.CloudFormationMappingSecondLevelKeyReference;
import com.intellij.aws.cloudformation.references.CloudFormationMappingTopLevelKeyReference;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CloudFormationReferenceProvider extends PsiReferenceProvider {
  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    if (!CloudFormationPsiUtils.isCloudFormationFile(element)) {
      return PsiReference.EMPTY_ARRAY;
    }

    final List<PsiReference> references = buildFromElement(element);
    if (references.isEmpty()) {
      return PsiReference.EMPTY_ARRAY;
    } else {
      PsiReference[] result = new PsiReference[references.size()];
      references.toArray(result);
      return result;
    }
  }

  @NotNull
  public static List<PsiReference> buildFromElement(PsiElement element) {
    final JSLiteralExpression literalExpression = ObjectUtils.tryCast(element, JSLiteralExpression.class);
    if (literalExpression == null) {
      return Collections.emptyList();
    }

    List<PsiReference> result = new ArrayList<PsiReference>();

    if (handleRef(literalExpression, result)) {
      return result;
    }

    if (isInCondition(literalExpression)) {
      result.add(new CloudFormationEntityReference(
        literalExpression,
        CloudFormationSections.Conditions));
      return result;
    }

    final JSArrayLiteralExpression parametersArray = ObjectUtils.tryCast(element.getParent(), JSArrayLiteralExpression.class);
    if (parametersArray != null) {
      final JSProperty funcProperty = ObjectUtils.tryCast(parametersArray.getParent(), JSProperty.class);
      final boolean isFindInMap = funcProperty != null && CloudFormationIntrinsicFunctions.FnFindInMap.equals(funcProperty.getName());
      final boolean isGetAtt = funcProperty != null && CloudFormationIntrinsicFunctions.FnGetAtt.equals(funcProperty.getName());
      final boolean isIf = funcProperty != null && CloudFormationIntrinsicFunctions.FnIf.equals(funcProperty.getName());

      if (isGetAtt || isFindInMap || isIf) {
        final JSObjectLiteralExpression obj = ObjectUtils.tryCast(funcProperty.getParent(), JSObjectLiteralExpression.class);
        if (obj != null) {
          final JSExpression[] allParameters = parametersArray.getExpressions();
          if (allParameters.length > 0 && element == allParameters[0]) {
            final JSProperty[] properties = obj.getProperties();
            if (properties.length == 1) {
              if (isGetAtt) {
                result.add(new CloudFormationEntityReference(literalExpression, CloudFormationSections.Resources));
                return result;
              }

              if (isFindInMap) {
                result.add(new CloudFormationEntityReference(literalExpression, CloudFormationSections.Mappings));
                return result;
              }

              if (isIf) {
                result.add(new CloudFormationEntityReference(literalExpression, CloudFormationSections.Conditions));
                return result;
              }
            }
          } else if (allParameters.length > 1 && element == allParameters[1]) {
            if (isFindInMap) {
              JSLiteralExpression mappingNameExpression = ObjectUtils.tryCast(allParameters[0], JSLiteralExpression.class);
              if (mappingNameExpression != null && mappingNameExpression.isQuotedLiteral()) {
                result.add(new CloudFormationMappingTopLevelKeyReference(literalExpression,
                                                                         CloudFormationResolve.object$.getTargetName(mappingNameExpression)));
                return result;
              }
            }
          } else if (allParameters.length > 2 && element == allParameters[2]) {
            if (isFindInMap) {
              JSLiteralExpression mappingNameExpression = ObjectUtils.tryCast(allParameters[0], JSLiteralExpression.class);
              JSLiteralExpression topLevelKeyExpression = ObjectUtils.tryCast(allParameters[1], JSLiteralExpression.class);
              if (mappingNameExpression != null &&
                  mappingNameExpression.isQuotedLiteral() &&
                  topLevelKeyExpression != null &&
                  topLevelKeyExpression.isQuotedLiteral()) {
                result.add(new CloudFormationMappingSecondLevelKeyReference(
                  literalExpression,
                  CloudFormationResolve.object$.getTargetName(mappingNameExpression),
                  CloudFormationResolve.object$.getTargetName(topLevelKeyExpression)));
                return result;
              }
            }
          }
        }
      }
    }

    if (handleDependsOnSingle(literalExpression, result)) {
      return result;
    }

    if (handleDependsOnMultiple(literalExpression, result)) {
      return result;
    }

    if (isInConditionOnResource(element)) {
      result.add(new CloudFormationEntityReference(
        literalExpression,
        CloudFormationSections.Conditions));
      return result;
    }

    return result;
  }

  public static boolean handleRef(JSLiteralExpression element, List<PsiReference> result) {
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

    final String targetName = CloudFormationResolve.object$.getTargetName(element);
    if (CloudFormationMetadataProvider.METADATA.predefinedParameters.contains(targetName)) {
      return false;
    }

    result.add(new CloudFormationEntityReference(
      element,
      CloudFormationSections.Parameters,
      CloudFormationSections.Resources));
    return true;
  }

  public static boolean isInCondition(JSLiteralExpression element) {
    final JSProperty conditionProperty = ObjectUtils.tryCast(element.getParent(), JSProperty.class);
    if (conditionProperty == null || !CloudFormationConstants.Condition.equals(conditionProperty.getName())) {
      return false;
    }

    final JSObjectLiteralExpression obj = ObjectUtils.tryCast(conditionProperty.getParent(), JSObjectLiteralExpression.class);
    return obj != null && obj.getProperties().length == 1;
  }

  public static boolean handleDependsOnSingle(JSLiteralExpression element, List<PsiReference> result) {
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
    if (resource == null || !isResourceElement(resource)) {
      return false;
    }

    result.add(new CloudFormationEntityReference(
      element,
      Arrays.asList(resource.getName()),
      CloudFormationSections.Resources));
    return true;
  }

  public static boolean isInConditionOnResource(PsiElement element) {
    final JSProperty conditionProperty = ObjectUtils.tryCast(element.getParent(), JSProperty.class);
    if (conditionProperty == null || !CloudFormationConstants.Condition.equals(conditionProperty.getName())) {
      return false;
    }

    final JSObjectLiteralExpression resourceProperties =
      ObjectUtils.tryCast(conditionProperty.getParent(), JSObjectLiteralExpression.class);
    if (resourceProperties == null) {
      return false;
    }

    final JSProperty resource = ObjectUtils.tryCast(resourceProperties.getParent(), JSProperty.class);
    return resource != null && isResourceElement(resource);
  }

  public static boolean handleDependsOnMultiple(JSLiteralExpression element, List<PsiReference> result) {
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
    if (resource == null || !isResourceElement(resource)) {
      return false;
    }

    Collection<String> excludes = new HashSet<String>();
    for (JSExpression childExpression : refArray.getExpressions()) {
      if (childExpression == element) {
        continue;
      }

      if (childExpression instanceof JSLiteralExpression) {
        excludes.add(StringUtil.unquoteString(StringUtil.notNullize(childExpression.getText())));
      }
    }

    excludes.add(resource.getName());

    result.add(new CloudFormationEntityReference(
      element,
      excludes,
      CloudFormationSections.Resources));
    return true;
  }

  private static boolean isResourceElement(JSProperty element) {
    JSObjectLiteralExpression resourcesProperties = ObjectUtils.tryCast(element.getParent(), JSObjectLiteralExpression.class);
    if (resourcesProperties == null) {
      return false;
    }

    JSProperty resourcesProperty = ObjectUtils.tryCast(resourcesProperties.getParent(), JSProperty.class);
    if (resourcesProperty == null || !CloudFormationSections.Resources.equals(resourcesProperty.getName())) {
      return false;
    }

    return CloudFormationPsiUtils.getRootExpression(resourcesProperty.getContainingFile()) == resourcesProperty.getParent();
  }
}
