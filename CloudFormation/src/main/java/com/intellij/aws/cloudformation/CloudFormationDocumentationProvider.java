package com.intellij.aws.cloudformation;

import com.intellij.aws.cloudformation.metadata.CloudFormationResourceProperty;
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceType;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CloudFormationDocumentationProvider extends AbstractDocumentationProvider {
  @Nullable
  private static PsiElement getDocElement(PsiElement element) {
    if (!CloudFormationPsiUtils.isCloudFormationFile(element)) {
      return null;
    }

    PsiElement parent = element;
    if (parent.getParent() instanceof JsonStringLiteral) {
      parent = parent.getParent();
    }

    if (CloudFormationPsiUtils.isResourceTypeValuePosition(parent) || CloudFormationPsiUtils.isResourcePropertyNamePosition(parent)) {
      return parent;
    }

    return null;
  }

  @Override
  public String generateDoc(final PsiElement element, @Nullable final PsiElement originalElement) {
    PsiElement docElement = getDocElement(originalElement);
    if (docElement == null) {
      return null;
    }

    if (CloudFormationPsiUtils.isResourceTypeValuePosition(docElement)) {
      return createResourceDescription(docElement);
    } else if (CloudFormationPsiUtils.isResourcePropertyNamePosition(docElement)) {
      return createPropertyDescription(docElement);
    }

    return null;
  }

  private String createResourceDescription(PsiElement element) {
    final JsonStringLiteral propertyValue = ObjectUtils.tryCast(element, JsonStringLiteral.class);
    if (propertyValue == null) {
      return "";
    }

    final JsonProperty property = ObjectUtils.tryCast(element.getParent(), JsonProperty.class);
    if (property == null || !property.getName().equals("Type")) {
      return "";
    }

    for (CloudFormationResourceType resourceType : CloudFormationMetadataProvider.METADATA.resourceTypes) {
      String propertyText = property.getValue() != null ? property.getValue().getText() : "";

      if (resourceType.name.equals(propertyText.replace("\"", "")))
        return prefixLinksWithUserGuideRoot(resourceType.description);
    }

    return "";
  }

  private String createPropertyDescription(PsiElement element) {
    final JsonStringLiteral propertyName = ObjectUtils.tryCast(element, JsonStringLiteral.class);
    if (propertyName == null) {
      return "";
    }

    final JsonProperty property = ObjectUtils.tryCast(propertyName.getParent(), JsonProperty.class);
    if (property == null || property.getNameElement() != propertyName) {
      return "";
    }

    final JsonProperty resourceElement = CloudFormationPsiUtils.getResourceElementFromPropertyName(propertyName);
    if (resourceElement == null) {
      return "";
    }

    final JsonObject resourceValue = ObjectUtils.tryCast(resourceElement.getValue(), JsonObject.class);
    if (resourceValue == null) {
      return "";
    }

    final JsonProperty typeProperty = resourceValue.findProperty(CloudFormationConstants.TypePropertyName);
    if (typeProperty == null) {
      return "";
    }

    final JsonStringLiteral typeValue = ObjectUtils.tryCast(typeProperty.getValue(), JsonStringLiteral.class);
    if (typeValue == null) {
      return "";
    }

    final String type = CloudFormationResolve.Companion.getTargetName(typeValue);

    final CloudFormationResourceType resourceTypeMetadata = CloudFormationMetadataProvider.METADATA.findResourceType(type);
    if (resourceTypeMetadata == null) {
      return "";
    }

    for (CloudFormationResourceProperty propertyMetadata : resourceTypeMetadata.properties) {
      if (propertyMetadata.name.equals(property.getName())) {
        String document = "<p>" + propertyMetadata.description + "</p><br>" +
            "<p><i>Required:</i> " + propertyMetadata.required + "</p>" +
            propertyMetadata.type +
            propertyMetadata.updateRequires;

        return prefixLinksWithUserGuideRoot(document);
      }

    }
    return "";
  }

  @Nullable
  public PsiElement getCustomDocumentationElement(@NotNull Editor editor, @NotNull PsiFile file, @Nullable PsiElement contextElement) {
    return getDocElement(contextElement) != null ? contextElement : null;
  }

  private static String prefixLinksWithUserGuideRoot(String html) {
    return html.replaceAll("href=\"(?!http)", "href=\"http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/");
  }
}