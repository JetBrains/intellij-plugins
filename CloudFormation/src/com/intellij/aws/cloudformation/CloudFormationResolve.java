package com.intellij.aws.cloudformation;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class CloudFormationResolve {
  @Nullable
  public static JSObjectLiteralExpression getSectionNode(final PsiFile file, String name) {
    return CloudFormationPsiUtils.getObjectLiteralExpressionChild(CloudFormationPsiUtils.getRootExpression(file), name);
  }

  @NotNull
  public static String getTargetName(@NotNull JSLiteralExpression element) {
    return StringUtil.stripQuotesAroundValue(StringUtil.notNullize(element.getText()));
  }

  @Nullable
  public static JSProperty resolveEntity(PsiFile file, String entityName, String... sections) {
    for (String sectionName : sections) {
      final JSObjectLiteralExpression section = getSectionNode(file, sectionName);
      if (section != null) {
        final JSProperty property = section.findProperty(entityName);
        if (property != null) {
          return property;
        }
      }
    }

    return null;
  }

  public static String[] getEntities(PsiFile file, String[] sections) {
    Set<String> result = new HashSet<String>();

    for (String sectionName : sections) {
      final JSObjectLiteralExpression section = getSectionNode(file, sectionName);
      if (section != null) {
        for (JSProperty property : section.getProperties()) {
          result.add(property.getName());
        }
      }
    }

    return ArrayUtil.toStringArray(result);
  }

  @Nullable
  public static JSProperty resolveTopLevelMappingKey(PsiFile file, String mappingName, String topLevelKey) {
    JSProperty mappingElement = resolveEntity(file, mappingName, CloudFormationSections.Mappings);
    if (mappingElement == null) {
      return null;
    }

    final JSObjectLiteralExpression objectLiteralExpression =
      ObjectUtils.tryCast(mappingElement.getValue(), JSObjectLiteralExpression.class);
    if (objectLiteralExpression == null) {
      return null;
    }

    return objectLiteralExpression.findProperty(topLevelKey);
  }

  @Nullable
  public static PsiElement resolveSecondLevelMappingKey(PsiFile file, String mappingName, String topLevelKey, String secondLevelKey) {
    JSProperty topLevelKeyElement = resolveTopLevelMappingKey(file, mappingName, topLevelKey);
    if (topLevelKeyElement == null) {
      return null;
    }

    final JSObjectLiteralExpression objectLiteralExpression =
      ObjectUtils.tryCast(topLevelKeyElement.getValue(), JSObjectLiteralExpression.class);
    if (objectLiteralExpression == null) {
      return null;
    }

    return objectLiteralExpression.findProperty(secondLevelKey);
  }

  @Nullable
  public static String[] getTopLevelMappingKeys(PsiFile file, String mappingName) {
    JSProperty mappingElement = resolveEntity(file, mappingName, CloudFormationSections.Mappings);
    if (mappingElement == null) {
      return null;
    }

    final JSObjectLiteralExpression objectLiteralExpression =
      ObjectUtils.tryCast(mappingElement.getValue(), JSObjectLiteralExpression.class);
    if (objectLiteralExpression == null) {
      return null;
    }

    return getPropertiesName(objectLiteralExpression.getProperties());
  }

  @Nullable
  public static String[] getSecondLevelMappingKeys(@NotNull PsiFile file, @NotNull String mappingName, @NotNull String topLevelKey) {
    final JSProperty topLevelKeyElement = resolveTopLevelMappingKey(file, mappingName, topLevelKey);
    if (topLevelKeyElement == null) {
      return null;
    }

    final JSObjectLiteralExpression objectLiteralExpression =
      ObjectUtils.tryCast(topLevelKeyElement.getValue(), JSObjectLiteralExpression.class);
    if (objectLiteralExpression == null) {
      return null;
    }

    return getPropertiesName(objectLiteralExpression.getProperties());
  }

  @Nullable
  private static String[] getPropertiesName(@Nullable JSProperty[] properties) {
    if (properties == null) {
      return null;
    }

    Set<String> result = new HashSet<String>();

    for (JSProperty property : properties) {
      result.add(property.getName());
    }

    return ArrayUtil.toStringArray(result);
  }
}
