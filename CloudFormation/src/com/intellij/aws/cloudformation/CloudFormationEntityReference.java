package com.intellij.aws.cloudformation;

import com.intellij.lang.javascript.json.JSONLanguageDialect;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CloudFormationEntityReference extends PsiReferenceBase<JSLiteralExpression> {
  private final String[] myPossibleSections;

  private CloudFormationEntityReference(@NotNull JSLiteralExpression element, String... possibleSections) {
    super(element);

    assert possibleSections.length > 0;
    myPossibleSections = possibleSections;
  }

  @Override
  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    final JSArrayLiteralExpression arrayElement =
      (JSArrayLiteralExpression)JSChangeUtil.createJSTreeFromText(
        myElement.getProject(), "[\"" + newElementName + "\"]",
        JSONLanguageDialect.JSON).getPsi();
    final JSExpression newElement = arrayElement.getExpressions()[0];
    return myElement.replace(newElement);
  }

  @Nullable
  @Override
  public PsiElement resolve() {
    final String entityName = StringUtil.stripQuotesAroundValue(StringUtil.notNullize(myElement.getText()));
    return CloudFormationResolve.resolveEntity(myElement.getContainingFile(), entityName, myPossibleSections);
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    return CloudFormationResolve.getEntities(myElement.getContainingFile(), myPossibleSections);
  }

  private static String getTargetName(JSLiteralExpression element) {
    return StringUtil.stripQuotesAroundValue(StringUtil.notNullize(element.getText()));
  }

  @Nullable
  public static CloudFormationEntityReference buildFromElement(PsiElement element) {
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

  private static boolean isInRef(JSLiteralExpression element) {
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

    final String targetName = getTargetName(element);
    return !CloudFormationConstants.PredefinedParameters.contains(targetName);
  }

  private static boolean isInDependsOnSingle(PsiElement element) {
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

  private static boolean isInDependsOnMultiple(PsiElement element) {
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
