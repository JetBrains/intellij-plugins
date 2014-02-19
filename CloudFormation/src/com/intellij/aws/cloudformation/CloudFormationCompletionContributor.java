package com.intellij.aws.cloudformation;

import com.intellij.aws.cloudformation.metadata.CloudFormationResourceType;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class CloudFormationCompletionContributor extends CompletionContributor {
  public CloudFormationCompletionContributor() {
    extend(CompletionType.BASIC,
           PlatformPatterns.psiElement().withLanguage(JavascriptLanguage.INSTANCE),
           new CompletionProvider<CompletionParameters>() {
             public void addCompletions(@NotNull CompletionParameters parameters,
                                        ProcessingContext context,
                                        @NotNull CompletionResultSet resultSet) {
               final PsiElement position = parameters.getPosition();

               if (!CloudFormationPsiUtils.isCloudFormationFile(position)) {
                 return;
               }

               PrefixMatcher oldPrefixMatcher = resultSet.getPrefixMatcher();
               CompletionResultSet rs = resultSet.withPrefixMatcher(new PlainPrefixMatcher(oldPrefixMatcher.getPrefix()));

               PsiElement parent = position.getParent();
               boolean quoteResult = false; // parent instanceof JSReferenceExpression;

               if (isResourceTypeValuePosition(parent)) {
                 for (CloudFormationResourceType resourceType : CloudFormationMetadataProvider.METADATA.resourceTypes) {
                   rs.addElement(createLookupElement(resourceType.name, quoteResult));
                 }
               }

               for (PsiReference reference : parent.getReferences()) {
                 if (reference instanceof CloudFormationEntityReference) {
                   CloudFormationEntityReference entityRef = (CloudFormationEntityReference)reference;
                   for (String v : entityRef.getCompletionVariants()) {
                     rs.addElement(createLookupElement(v, quoteResult));
                   }
                 }
               }

               // Disable all other items from JavaScript
               rs.stopHere();
             }
           }
    );
  }

  private LookupElement createLookupElement(String val, boolean quote) {
    String id = quote ? ("\"" + val + "\"") : val;
    return LookupElementBuilder.create(id);
  }

  private boolean isResourceTypeValuePosition(PsiElement position) {
    final JSExpression valueExpression = ObjectUtils.tryCast(position, JSExpression.class);
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
