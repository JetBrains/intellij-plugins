package com.intellij.aws.cloudformation;

import com.intellij.json.psi.JsonLiteral;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.Nullable;

public class CloudFormationPsiUtils {
  public static boolean isCloudFormationFile(final PsiElement element) {
    return element.getContainingFile().getFileType() == CloudFormationFileType.INSTANCE;
  }

  @Nullable
  public static JsonObject getRootExpression(final PsiFile file) {
    for (PsiElement cur = file.getFirstChild(); cur != null; cur = cur.getNextSibling()) {
      if (cur instanceof JsonObject) {
        return (JsonObject) cur;
      }
    }

    return null;
  }

  @Nullable
  public static JsonObject getObjectLiteralExpressionChild(@Nullable JsonObject parent, String childName) {
    if (parent == null) {
      return null;
    }

    final JsonProperty property = parent.findProperty(childName);
    if (property == null) {
      return null;
    }

    return ObjectUtils.tryCast(property.getValue(), JsonObject.class);
  }

  public static boolean isResourceTypeValuePosition(PsiElement position) {
    final JsonLiteral valueExpression = ObjectUtils.tryCast(position, JsonLiteral.class);
    if (valueExpression == null) {
      return false;
    }

    final JsonProperty typeProperty = ObjectUtils.tryCast(valueExpression.getParent(), JsonProperty.class);
    if (typeProperty == null ||
        typeProperty.getValue() != valueExpression ||
        !CloudFormationConstants.TypePropertyName.equals(typeProperty.getName())) {
      return false;
    }

    final JsonObject resourceExpression = ObjectUtils.tryCast(typeProperty.getParent(), JsonObject.class);
    if (resourceExpression == null) {
      return false;
    }

    final JsonProperty resourceProperty = ObjectUtils.tryCast(resourceExpression.getParent(), JsonProperty.class);
    if (resourceProperty == null) {
      return false;
    }

    final JsonObject resourcesExpression =
        ObjectUtils.tryCast(resourceProperty.getParent(), JsonObject.class);
    if (resourcesExpression == null) {
      return false;
    }

    final JsonProperty resourcesProperty = ObjectUtils.tryCast(resourcesExpression.getParent(), JsonProperty.class);
    if (resourcesProperty == null ||
        !CloudFormationSections.Resources.equals(StringUtil.stripQuotesAroundValue(resourcesProperty.getName()))) {
      return false;
    }

    final JsonObject root = CloudFormationPsiUtils.getRootExpression(resourceProperty.getContainingFile());
    return root == resourcesProperty.getParent();
  }

  public static boolean isResourcePropertyNamePosition(PsiElement position) {
    final JsonProperty resourceProperty = getResourceElementFromPropertyName(position);
    if (resourceProperty == null) {
      return false;
    }

    final JsonObject resourcesExpression =
        ObjectUtils.tryCast(resourceProperty.getParent(), JsonObject.class);
    if (resourcesExpression == null) {
      return false;
    }

    final JsonProperty resourcesProperty = ObjectUtils.tryCast(resourcesExpression.getParent(), JsonProperty.class);
    if (resourcesProperty == null ||
        resourcesProperty.getName().isEmpty() ||
        !CloudFormationSections.Resources.equals(StringUtil.stripQuotesAroundValue(resourcesProperty.getName()))) {
      return false;
    }

    final JsonObject root = CloudFormationPsiUtils.getRootExpression(resourceProperty.getContainingFile());
    return root == resourcesProperty.getParent();
  }
  @Nullable
  public static JsonProperty getResourceElementFromPropertyName(PsiElement position) {
    final JsonStringLiteral propertyName = ObjectUtils.tryCast(position, JsonStringLiteral.class);
    if (propertyName == null) {
      return null;
    }

    final JsonProperty property = ObjectUtils.tryCast(propertyName.getParent(), JsonProperty.class);
    if (property == null || property.getNameElement() != propertyName) {
      return null;
    }

    final JsonObject propertiesExpression = ObjectUtils.tryCast(property.getParent(), JsonObject.class);
    if (propertiesExpression == null) {
      return null;
    }

    final JsonProperty properties = ObjectUtils.tryCast(propertiesExpression.getParent(), JsonProperty.class);
    if (properties == null ||
        properties.getValue() != propertiesExpression ||
        !CloudFormationConstants.PropertiesPropertyName.equals(properties.getName())) {
      return null;
    }

    final JsonObject resourceExpression = ObjectUtils.tryCast(properties.getParent(), JsonObject.class);
    if (resourceExpression == null) {
      return null;
    }

    return ObjectUtils.tryCast(resourceExpression.getParent(), JsonProperty.class);
  }


}
