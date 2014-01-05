package com.intellij.aws.cloudformation;

import com.intellij.aws.cloudformation.metadata.CloudFormationResourceType;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class CloudFormationResourceTypeCompletionContributor extends CompletionContributor {
  public CloudFormationResourceTypeCompletionContributor() {
    extend(CompletionType.BASIC,
           PlatformPatterns.psiElement().withLanguage(JavascriptLanguage.INSTANCE),
           new CompletionProvider<CompletionParameters>() {
             public void addCompletions(@NotNull CompletionParameters parameters,
                                        ProcessingContext context,
                                        @NotNull CompletionResultSet resultSet) {
               final PsiElement position = parameters.getPosition();

               if (!CloudFormationPsiUtils.isCloudFormationFile(position) || !isResourceTypeValuePosition(position)) {
                 return;
               }

               for (CloudFormationResourceType resourceType : CloudFormationMetadataProvider.METADATA.resourceTypes) {
                 resultSet.addElement(LookupElementBuilder.create(resourceType.name));
               }
             }
           }
    );
  }

  private boolean isResourceTypeValuePosition(PsiElement position) {
    final JSLiteralExpression valueExpression = ObjectUtils.tryCast(position.getParent(), JSLiteralExpression.class);
    if (valueExpression == null) {
      return false;
    }

    final JSProperty typeProperty = ObjectUtils.tryCast(valueExpression.getParent(), JSProperty.class);
    if (typeProperty == null || !CloudFormationConstants.TypePropertyName.equals(typeProperty.getName())) {
      return false;
    }

    final JSObjectLiteralExpression resourceExpression = ObjectUtils.tryCast(typeProperty.getParent(), JSObjectLiteralExpression.class);
    if (resourceExpression == null) {
      return false;
    }

    final JSProperty resourceProperty = ObjectUtils.tryCast(resourceExpression.getParent(), JSProperty.class);
    if (resourceProperty == null) {
      return false;
    }

    final JSObjectLiteralExpression resourcesExpression =
      ObjectUtils.tryCast(resourceProperty.getParent(), JSObjectLiteralExpression.class);
    if (resourcesExpression == null) {
      return false;
    }

    final JSProperty resourcesProperty = ObjectUtils.tryCast(resourcesExpression.getParent(), JSProperty.class);
    if (resourcesProperty == null ||
        resourcesProperty.getName() == null ||
        !CloudFormationSections.Resources.equals(StringUtil.stripQuotesAroundValue(resourcesProperty.getName()))) {
      return false;
    }

    final JSObjectLiteralExpression root = CloudFormationPsiUtils.getRootExpression(resourceProperty.getContainingFile());
    return root == resourcesProperty.getParent();
  }
}
