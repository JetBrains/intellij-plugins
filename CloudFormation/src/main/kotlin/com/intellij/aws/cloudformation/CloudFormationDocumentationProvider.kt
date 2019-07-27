package com.intellij.aws.cloudformation

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.yaml.psi.YAMLScalar

class CloudFormationDocumentationProvider : AbstractDocumentationProvider() {
  override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
    val docElement = getDocElement(originalElement) ?: return null
    val parsed = CloudFormationParser.parse(docElement.containingFile)

    val resourceTypeValueMatch = ResourceTypeValueMatch.match(docElement, parsed)
    if (resourceTypeValueMatch != null) {
      return createResourceDescription(resourceTypeValueMatch)
    }

    val resourcePropertyNameMatch = ResourcePropertyNameMatch.match(docElement, parsed)
    if (resourcePropertyNameMatch != null) {
      return createPropertyDescription(resourcePropertyNameMatch)
    }

    return null
  }

  override fun getCustomDocumentationElement(editor: Editor, file: PsiFile, contextElement: PsiElement?): PsiElement? {
    return getDocElement(contextElement)
  }

  override fun getUrlFor(element: PsiElement?, originalElement: PsiElement?): MutableList<String>? {
    val docElement = getDocElement(originalElement) ?: return null
    val parsed = CloudFormationParser.parse(docElement.containingFile)

    val resourceTypeValueMatch = ResourceTypeValueMatch.match(docElement, parsed)
    if (resourceTypeValueMatch != null) {
      val typeName = resourceTypeValueMatch.resource.typeName ?: return null
      val resourceType = CloudFormationMetadataProvider.METADATA.resourceTypes[typeName] ?: return null
      return if (resourceType.url.isBlank()) null else mutableListOf(resourceType.url)
    }

    val resourcePropertyNameMatch = ResourcePropertyNameMatch.match(docElement, parsed)
    if (resourcePropertyNameMatch != null) {
      val typeName = resourcePropertyNameMatch.resource.typeName ?: return null
      val resourceTypeMetadata = CloudFormationMetadataProvider.METADATA.resourceTypes[typeName] ?: return null
      val propertyMetadata = resourceTypeMetadata.properties[resourcePropertyNameMatch.name.value] ?: return null
      return if (propertyMetadata.url.isBlank()) null else mutableListOf(propertyMetadata.url)
    }

    return null
  }

  private fun getDocElement(originalElement: PsiElement?): PsiElement? {
    if (originalElement == null) return null
    if (!CloudFormationPsiUtils.isCloudFormationFile(originalElement)) {
      return null
    }

    return if (originalElement.parent is JsonStringLiteral || originalElement.parent is YAMLScalar) originalElement.parent else originalElement
  }

  private fun createResourceDescription(match: ResourceTypeValueMatch): String? {
    val typeName = match.resource.typeName ?: return null
    val resourceType = CloudFormationMetadataProvider.DESCRIPTIONS.resourceTypes[typeName] ?: return null
    return resourceType.description
  }

  private fun createPropertyDescription(match: ResourcePropertyNameMatch): String? {
    val typeName = match.resource.typeName ?: return null

    val resourceTypeMetadata = CloudFormationMetadataProvider.METADATA.resourceTypes[typeName] ?: return ""
    val propertyMetadata = resourceTypeMetadata.properties[match.name.value] ?: return ""

    val resourceTypeDescription = CloudFormationMetadataProvider.DESCRIPTIONS.resourceTypes[typeName] ?: return ""
    val propertyDescription = resourceTypeDescription.properties[match.name.value] ?: return ""

    val document =
        "<h1>${propertyMetadata.name} ($typeName)</h1>" +
        propertyDescription +
        "<p><i>Required:</i> " + propertyMetadata.required + "</p>" +
        propertyMetadata.type +
        propertyMetadata.updateRequires

    return document
  }
}