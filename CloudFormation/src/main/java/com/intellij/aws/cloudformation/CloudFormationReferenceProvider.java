package com.intellij.aws.cloudformation;

import com.intellij.aws.cloudformation.references.CloudFormationEntityReference;
import com.intellij.aws.cloudformation.references.CloudFormationMappingSecondLevelKeyReference;
import com.intellij.aws.cloudformation.references.CloudFormationMappingTopLevelKeyReference;
import com.intellij.json.psi.*;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CloudFormationReferenceProvider extends PsiReferenceProvider {
  public static final List<String> ParametersAndResourcesSections = Arrays.asList(CloudFormationSections.Parameters, CloudFormationSections.Resources);

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
    final JsonStringLiteral stringLiteral = ObjectUtils.tryCast(element, JsonStringLiteral.class);
    if (stringLiteral == null) {
      return Collections.emptyList();
    }

    List<PsiReference> result = new ArrayList<PsiReference>();

    if (handleRef(stringLiteral, result)) {
      return result;
    }

    if (isInCondition(stringLiteral)) {
      result.add(new CloudFormationEntityReference(stringLiteral, CloudFormationSections.ConditionsSingletonList, null));
      return result;
    }

    final JsonArray parametersArray = ObjectUtils.tryCast(element.getParent(), JsonArray.class);
    if (parametersArray != null) {
      final JsonProperty funcProperty = ObjectUtils.tryCast(parametersArray.getParent(), JsonProperty.class);
      final boolean isFindInMap = funcProperty != null && CloudFormationIntrinsicFunctions.FnFindInMap.equals(funcProperty.getName());
      final boolean isGetAtt = funcProperty != null && CloudFormationIntrinsicFunctions.FnGetAtt.equals(funcProperty.getName());
      final boolean isIf = funcProperty != null && CloudFormationIntrinsicFunctions.FnIf.equals(funcProperty.getName());

      if (isGetAtt || isFindInMap || isIf) {
        final JsonObject obj = ObjectUtils.tryCast(funcProperty.getParent(), JsonObject.class);
        if (obj != null) {
          final List<JsonValue> allParameters = parametersArray.getValueList();
          if (allParameters.size() > 0 && element == allParameters.get(0)) {
            final List<JsonProperty> properties = obj.getPropertyList();
            if (properties.size() == 1) {
              if (isGetAtt) {
                result.add(new CloudFormationEntityReference(stringLiteral, CloudFormationSections.ResourcesSingletonList, null));
                return result;
              }

              if (isFindInMap) {
                result.add(new CloudFormationEntityReference(stringLiteral, CloudFormationSections.MappingsSingletonList, null));
                return result;
              }

              if (isIf) {
                result.add(new CloudFormationEntityReference(stringLiteral, CloudFormationSections.ConditionsSingletonList, null));
                return result;
              }
            }
          } else if (allParameters.size() > 1 && element == allParameters.get(1)) {
            if (isFindInMap) {
              JsonStringLiteral mappingNameExpression = ObjectUtils.tryCast(allParameters.get(0), JsonStringLiteral.class);
              if (mappingNameExpression != null) {
                result.add(new CloudFormationMappingTopLevelKeyReference(stringLiteral,
                    CloudFormationResolve.Companion.getTargetName(mappingNameExpression)));
                return result;
              }
            }
          } else if (allParameters.size() > 2 && element == allParameters.get(2)) {
            if (isFindInMap) {
              JsonStringLiteral mappingNameExpression = ObjectUtils.tryCast(allParameters.get(0), JsonStringLiteral.class);
              JsonStringLiteral topLevelKeyExpression = ObjectUtils.tryCast(allParameters.get(1), JsonStringLiteral.class);
              if (mappingNameExpression != null && topLevelKeyExpression != null) {
                result.add(new CloudFormationMappingSecondLevelKeyReference(
                    stringLiteral,
                    CloudFormationResolve.Companion.getTargetName(mappingNameExpression),
                    CloudFormationResolve.Companion.getTargetName(topLevelKeyExpression)));
                return result;
              }
            }
          }
        }
      }
    }

    if (handleDependsOnSingle(stringLiteral, result)) {
      return result;
    }

    if (handleDependsOnMultiple(stringLiteral, result)) {
      return result;
    }

    if (isInConditionOnResource(element)) {
      result.add(new CloudFormationEntityReference(stringLiteral, CloudFormationSections.ConditionsSingletonList, null));
      return result;
    }

    return result;
  }

  public static boolean handleRef(JsonStringLiteral element, List<PsiReference> result) {
    final JsonProperty refProperty = ObjectUtils.tryCast(element.getParent(), JsonProperty.class);
    if (refProperty == null || !CloudFormationIntrinsicFunctions.Ref.equals(refProperty.getName())) {
      return false;
    }

    if (refProperty.getNameElement() == element) {
      return false;
    }

    final JsonObject obj = ObjectUtils.tryCast(refProperty.getParent(), JsonObject.class);
    if (obj == null) {
      return false;
    }

    final List<JsonProperty> properties = obj.getPropertyList();
    if (properties.size() != 1) {
      return false;
    }

    final String targetName = CloudFormationResolve.Companion.getTargetName(element);
    if (CloudFormationMetadataProvider.METADATA.predefinedParameters.contains(targetName)) {
      return false;
    }

    result.add(new CloudFormationEntityReference(element, ParametersAndResourcesSections, null));
    return true;
  }

  public static boolean isInCondition(JsonLiteral element) {
    final JsonProperty conditionProperty = ObjectUtils.tryCast(element.getParent(), JsonProperty.class);
    if (conditionProperty == null || !CloudFormationConstants.ConditionPropertyName.equals(conditionProperty.getName())) {
      return false;
    }

    if (conditionProperty.getNameElement() == element) {
      return false;
    }

    final JsonObject obj = ObjectUtils.tryCast(conditionProperty.getParent(), JsonObject.class);
    return obj != null && obj.getPropertyList().size() == 1;
  }

  public static boolean handleDependsOnSingle(JsonLiteral element, List<PsiReference> result) {
    final JsonProperty dependsOnProperty = ObjectUtils.tryCast(element.getParent(), JsonProperty.class);
    if (dependsOnProperty == null || !CloudFormationConstants.DependsOnPropertyName.equals(dependsOnProperty.getName())) {
      return false;
    }

    if (dependsOnProperty.getNameElement() == element) {
      return false;
    }

    final JsonObject resourceProperties = ObjectUtils.tryCast(dependsOnProperty.getParent(), JsonObject.class);
    if (resourceProperties == null) {
      return false;
    }

    final JsonProperty resource = ObjectUtils.tryCast(resourceProperties.getParent(), JsonProperty.class);
    if (resource == null || !isResourceElement(resource)) {
      return false;
    }

    result.add(new CloudFormationEntityReference(
        element, CloudFormationSections.ResourcesSingletonList, Collections.singletonList(resource.getName())));
    return true;
  }

  public static boolean isInConditionOnResource(PsiElement element) {
    final JsonProperty conditionProperty = ObjectUtils.tryCast(element.getParent(), JsonProperty.class);
    if (conditionProperty == null || !CloudFormationConstants.ConditionPropertyName.equals(conditionProperty.getName())) {
      return false;
    }

    if (conditionProperty.getNameElement() == element) {
      return false;
    }

    final JsonObject resourceProperties =
        ObjectUtils.tryCast(conditionProperty.getParent(), JsonObject.class);
    if (resourceProperties == null) {
      return false;
    }

    final JsonProperty resource = ObjectUtils.tryCast(resourceProperties.getParent(), JsonProperty.class);
    return resource != null && isResourceElement(resource);
  }

  public static boolean handleDependsOnMultiple(JsonLiteral element, List<PsiReference> result) {
    final JsonArray refArray = ObjectUtils.tryCast(element.getParent(), JsonArray.class);
    if (refArray == null) {
      return false;
    }

    final JsonProperty dependsOnProperty = ObjectUtils.tryCast(refArray.getParent(), JsonProperty.class);
    if (dependsOnProperty == null || !CloudFormationConstants.DependsOnPropertyName.equals(dependsOnProperty.getName())) {
      return false;
    }

    final JsonObject resourceProperties =
        ObjectUtils.tryCast(dependsOnProperty.getParent(), JsonObject.class);
    if (resourceProperties == null) {
      return false;
    }

    final JsonProperty resource = ObjectUtils.tryCast(resourceProperties.getParent(), JsonProperty.class);
    if (resource == null || !isResourceElement(resource)) {
      return false;
    }

    Collection<String> excludes = new HashSet<String>();
    for (JsonValue childExpression : refArray.getValueList()) {
      if (childExpression == element) {
        continue;
      }

      if (childExpression instanceof JsonLiteral) {
        excludes.add(StringUtil.unquoteString(StringUtil.notNullize(childExpression.getText())));
      }
    }

    excludes.add(resource.getName());

    result.add(new CloudFormationEntityReference(element, CloudFormationSections.ResourcesSingletonList, excludes));
    return true;
  }

  private static boolean isResourceElement(JsonProperty element) {
    JsonObject resourcesProperties = ObjectUtils.tryCast(element.getParent(), JsonObject.class);
    if (resourcesProperties == null) {
      return false;
    }

    JsonProperty resourcesProperty = ObjectUtils.tryCast(resourcesProperties.getParent(), JsonProperty.class);
    if (resourcesProperty == null || !CloudFormationSections.Resources.equals(resourcesProperty.getName())) {
      return false;
    }

    return CloudFormationPsiUtils.getRootExpression(resourcesProperty.getContainingFile()) == resourcesProperty.getParent();
  }
}
