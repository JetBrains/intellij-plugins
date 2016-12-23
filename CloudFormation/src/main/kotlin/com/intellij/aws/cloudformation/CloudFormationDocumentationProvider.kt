package com.intellij.aws.cloudformation

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement

class CloudFormationDocumentationProvider : AbstractDocumentationProvider() {
  override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
    if (originalElement == null) return null
    if (!CloudFormationPsiUtils.isCloudFormationFile(originalElement)) {
      return null
    }

    val docElement = if (originalElement.parent is JsonStringLiteral) originalElement.parent else originalElement
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

    val document = "<p>" + propertyDescription + "</p><br>" +
        "<p><i>Required:</i> " + propertyMetadata.required + "</p>" +
        propertyMetadata.type +
        propertyMetadata.updateRequires

    return prefixLinksWithUserGuideRoot(document)
  }

  private fun prefixLinksWithUserGuideRoot(html: String): String {
    return html.replace("href=\"(?!http)".toRegex(), "href=\"http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/")
  }
}