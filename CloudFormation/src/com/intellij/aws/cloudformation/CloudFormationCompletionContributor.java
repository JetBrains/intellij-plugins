package com.intellij.aws.cloudformation;

import com.intellij.aws.cloudformation.metadata.CloudFormationResourceAttribute;
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceProperty;
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceType;
import com.intellij.aws.cloudformation.references.CloudFormationReferenceBase;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
                 completeResourceType(rs, quoteResult);
               } else if (isResourcePropertyNamePosition(parent)) {
                 completeResourceProperty(rs, parent, quoteResult);
               }

               String attResourceName = getResourceNameFromGetAttAtrributePosition(parent);
               if (attResourceName != null) {
                 completeAttribute(parent.getContainingFile(), rs, quoteResult, attResourceName);
               }

               for (PsiReference reference : parent.getReferences()) {
                 final CloudFormationReferenceBase cfnReference = ObjectUtils.tryCast(reference, CloudFormationReferenceBase.class);
                 if (cfnReference != null) {
                   for (String v : cfnReference.getCompletionVariants()) {
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

  private String getResourceNameFromGetAttAtrributePosition(PsiElement element) {
    final JSLiteralExpression attributeExpression = ObjectUtils.tryCast(element, JSLiteralExpression.class);
    if (attributeExpression == null || !attributeExpression.isQuotedLiteral()) {
      return null;
    }

    final JSArrayLiteralExpression getattParameters = ObjectUtils.tryCast(attributeExpression.getParent(), JSArrayLiteralExpression.class);
    if (getattParameters == null || getattParameters.getExpressions().length != 2) {
      return null;
    }

    final JSProperty getattProperty = ObjectUtils.tryCast(getattParameters.getParent(), JSProperty.class);
    if (getattProperty == null || !CloudFormationIntrinsicFunctions.FnGetAtt.equals(getattProperty.getName())) {
      return null;
    }

    final JSObjectLiteralExpression getattFunc = ObjectUtils.tryCast(getattProperty.getParent(), JSObjectLiteralExpression.class);
    if (getattFunc == null || getattFunc.getProperties().length != 1) {
      return null;
    }

    final String text = getattParameters.getExpressions()[0].getText();
    return StringUtil.stripQuotesAroundValue(text);
  }

  private void completeResourceType(CompletionResultSet rs, boolean quoteResult) {
    for (CloudFormationResourceType resourceType : CloudFormationMetadataProvider.METADATA.resourceTypes) {
      rs.addElement(createLookupElement(resourceType.name, quoteResult));
    }
  }

  private void completeAttribute(PsiFile file, CompletionResultSet rs, boolean quoteResult, String resourceName) {
    final JSProperty resource = CloudFormationResolve.object$.resolveEntity(file, resourceName, CloudFormationSections.Resources);
    if (resource == null) {
      return;
    }

    final JSObjectLiteralExpression resourceProperties = ObjectUtils.tryCast(resource.getValue(), JSObjectLiteralExpression.class);
    if (resourceProperties == null) {
      return;
    }

    final JSProperty typeProperty = resourceProperties.findProperty(CloudFormationConstants.TypePropertyName);
    if (typeProperty == null || typeProperty.getValue() == null) {
      return;
    }

    final String resourceTypeName = StringUtil.stripQuotesAroundValue(typeProperty.getValue().getText());

    final CloudFormationResourceType resourceType = CloudFormationMetadataProvider.METADATA.findResourceType(resourceTypeName);
    if (resourceType == null) {
      return;
    }

    for (CloudFormationResourceAttribute attribute : resourceType.attributes) {
      rs.addElement(createLookupElement(attribute.name, quoteResult));
    }
  }

  private void completeResourceProperty(CompletionResultSet rs, PsiElement parent, boolean quoteResult) {
    final JSProperty property = ObjectUtils.tryCast(parent, JSProperty.class);
    if (property == null) {
      return;
    }

    final JSObjectLiteralExpression propertiesExpression = ObjectUtils.tryCast(property.getParent(), JSObjectLiteralExpression.class);
    if (propertiesExpression == null) {
      return;
    }

    final JSProperty resourceElement = getResourceElementFromPropertyName(property);
    if (resourceElement == null) {
      return;
    }

    final JSObjectLiteralExpression resourceValue = ObjectUtils.tryCast(resourceElement.getValue(), JSObjectLiteralExpression.class);
    if (resourceValue == null) {
      return;
    }

    final JSProperty typeProperty = resourceValue.findProperty(CloudFormationConstants.TypePropertyName);
    if (typeProperty == null) {
      return;
    }

    final JSLiteralExpression typeValue = ObjectUtils.tryCast(typeProperty.getValue(), JSLiteralExpression.class);
    if (typeValue == null || !typeValue.isQuotedLiteral()) {
      return;
    }

    final String type = CloudFormationResolve.object$.getTargetName(typeValue);

    final CloudFormationResourceType resourceTypeMetadata = CloudFormationMetadataProvider.METADATA.findResourceType(type);
    if (resourceTypeMetadata == null) {
      return;
    }

    for (CloudFormationResourceProperty propertyMetadata : resourceTypeMetadata.properties) {
      if (propertiesExpression.findProperty(propertyMetadata.name) != null) {
        continue;
      }

      rs.addElement(createLookupElement(propertyMetadata.name, quoteResult));
    }
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
    if (typeProperty == null ||
        typeProperty.getValue() != valueExpression ||
        !CloudFormationConstants.TypePropertyName.equals(typeProperty.getName())) {
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

  private boolean isResourcePropertyNamePosition(PsiElement position) {
    final JSProperty resourceProperty = getResourceElementFromPropertyName(position);
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

  @Nullable
  private static JSProperty getResourceElementFromPropertyName(PsiElement position) {
    final JSProperty property = ObjectUtils.tryCast(position, JSProperty.class);
    if (property == null) {
      return null;
    }

    final JSObjectLiteralExpression propertiesExpression = ObjectUtils.tryCast(property.getParent(), JSObjectLiteralExpression.class);
    if (propertiesExpression == null) {
      return null;
    }

    final JSProperty properties = ObjectUtils.tryCast(propertiesExpression.getParent(), JSProperty.class);
    if (properties == null ||
        properties.getValue() != propertiesExpression ||
        !CloudFormationConstants.PropertiesPropertyName.equals(properties.getName())) {
      return null;
    }

    final JSObjectLiteralExpression resourceExpression = ObjectUtils.tryCast(properties.getParent(), JSObjectLiteralExpression.class);
    if (resourceExpression == null) {
      return null;
    }

    return ObjectUtils.tryCast(resourceExpression.getParent(), JSProperty.class);
  }
}
