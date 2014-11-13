package com.intellij.aws.cloudformation;

import com.intellij.aws.cloudformation.metadata.CloudFormationResourceAttribute;
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceProperty;
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceType;
import com.intellij.aws.cloudformation.references.CloudFormationReferenceBase;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.json.JsonLanguage;
import com.intellij.json.psi.*;
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
           PlatformPatterns.psiElement().withLanguage(JsonLanguage.INSTANCE),
           new CompletionProvider<CompletionParameters>() {
             public void addCompletions(@NotNull CompletionParameters parameters,
                                        ProcessingContext context,
                                        @NotNull CompletionResultSet rs) {
               final PsiElement position = parameters.getPosition();

               if (!CloudFormationPsiUtils.isCloudFormationFile(position)) {
                 return;
               }

               PsiElement parent = position.getParent();
               boolean quoteResult = false; // parent instanceof JSReferenceExpression;

               if (isResourceTypeValuePosition(parent)) {
                 completeResourceType(rs, quoteResult);
               } else if (isResourcePropertyNamePosition(parent)) {
                 completeResourceProperty(rs, parent, quoteResult);
               }

               completeResourceTopLevelProperty(rs, parent, quoteResult);

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

  private void completeResourceTopLevelProperty(CompletionResultSet rs, PsiElement element, boolean quoteResult) {
    final JsonProperty property = ObjectUtils.tryCast(element, JsonProperty.class);
    if (property == null) {
      return;
    }

    final JsonObject resourceExpression = ObjectUtils.tryCast(property.getParent(), JsonObject.class);
    if (resourceExpression == null) {
      return;
    }

    final JsonProperty resourceProperty = ObjectUtils.tryCast(resourceExpression.getParent(), JsonProperty.class);
    if (resourceProperty == null) {
      return;
    }

    final JsonObject resourcesExpression =
      ObjectUtils.tryCast(resourceProperty.getParent(), JsonObject.class);
    if (resourcesExpression == null) {
      return;
    }

    final JsonProperty resourcesProperty = ObjectUtils.tryCast(resourcesExpression.getParent(), JsonProperty.class);
    if (resourcesProperty == null ||
        !CloudFormationSections.Resources.equals(StringUtil.stripQuotesAroundValue(resourcesProperty.getName()))) {
      return;
    }

    final JsonObject root = CloudFormationPsiUtils.getRootExpression(resourceProperty.getContainingFile());
    if (root != resourcesProperty.getParent()) {
      return;
    }

    for (String propertyName : CloudFormationConstants.AllTopLevelResourceProperties) {
      if (resourceExpression.findProperty(propertyName) == null) {
        rs.addElement(createLookupElement(propertyName, quoteResult));
      }
    }
  }

  private String getResourceNameFromGetAttAtrributePosition(PsiElement element) {
    final JsonStringLiteral attributeExpression = ObjectUtils.tryCast(element, JsonStringLiteral.class);
    if (attributeExpression == null) {
      return null;
    }

    final JsonArray getattParameters = ObjectUtils.tryCast(attributeExpression.getParent(), JsonArray.class);
    if (getattParameters == null || getattParameters.getValueList().size() != 2) {
      return null;
    }

    final JsonProperty getattProperty = ObjectUtils.tryCast(getattParameters.getParent(), JsonProperty.class);
    if (getattProperty == null || !CloudFormationIntrinsicFunctions.FnGetAtt.equals(getattProperty.getName())) {
      return null;
    }

    final JsonObject getattFunc = ObjectUtils.tryCast(getattProperty.getParent(), JsonObject.class);
    if (getattFunc == null || getattFunc.getPropertyList().size() != 1) {
      return null;
    }

    final String text = getattParameters.getValueList().get(0).getText();
    return StringUtil.stripQuotesAroundValue(text);
  }

  private void completeResourceType(CompletionResultSet rs, boolean quoteResult) {
    for (CloudFormationResourceType resourceType : CloudFormationMetadataProvider.METADATA.resourceTypes) {
      rs.addElement(createLookupElement(resourceType.name, quoteResult));
    }
  }

  private void completeAttribute(PsiFile file, CompletionResultSet rs, boolean quoteResult, String resourceName) {
    final JsonProperty resource = CloudFormationResolve.OBJECT$.resolveEntity(file, resourceName, CloudFormationSections.Resources);
    if (resource == null) {
      return;
    }

    final JsonObject resourceProperties = ObjectUtils.tryCast(resource.getValue(), JsonObject.class);
    if (resourceProperties == null) {
      return;
    }

    final JsonProperty typeProperty = resourceProperties.findProperty(CloudFormationConstants.TypePropertyName);
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
    final JsonProperty property = ObjectUtils.tryCast(parent, JsonProperty.class);
    if (property == null) {
      return;
    }

    final JsonObject propertiesExpression = ObjectUtils.tryCast(property.getParent(), JsonObject.class);
    if (propertiesExpression == null) {
      return;
    }

    final JsonProperty resourceElement = getResourceElementFromPropertyName(property);
    if (resourceElement == null) {
      return;
    }

    final JsonObject resourceValue = ObjectUtils.tryCast(resourceElement.getValue(), JsonObject.class);
    if (resourceValue == null) {
      return;
    }

    final JsonProperty typeProperty = resourceValue.findProperty(CloudFormationConstants.TypePropertyName);
    if (typeProperty == null) {
      return;
    }

    final JsonStringLiteral typeValue = ObjectUtils.tryCast(typeProperty.getValue(), JsonStringLiteral.class);
    if (typeValue == null) {
      return;
    }

    final String type = CloudFormationResolve.OBJECT$.getTargetName(typeValue);

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

  private boolean isResourcePropertyNamePosition(PsiElement position) {
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
  private static JsonProperty getResourceElementFromPropertyName(PsiElement position) {
    final JsonProperty property = ObjectUtils.tryCast(position, JsonProperty.class);
    if (property == null) {
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
