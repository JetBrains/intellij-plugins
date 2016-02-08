package com.intellij.aws.cloudformation

import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

class CloudFormationDocumentationProvider : AbstractDocumentationProvider() {
  private fun getDocElement(element: PsiElement): PsiElement? {
    if (!CloudFormationPsiUtils.isCloudFormationFile(element)) {
      return null
    }

    var parent = element
    if (parent.parent is JsonStringLiteral) {
      parent = parent.parent
    }

    if (CloudFormationPsiUtils.isResourceTypeValuePosition(parent) || CloudFormationPsiUtils.isResourcePropertyNamePosition(parent)) {
      return parent
    }

    return null
  }

  override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
    if (originalElement == null) return null
    val docElement = getDocElement(originalElement) ?: return null

    if (CloudFormationPsiUtils.isResourceTypeValuePosition(docElement)) {
      return createResourceDescription(docElement)
    } else if (CloudFormationPsiUtils.isResourcePropertyNamePosition(docElement)) {
      return createPropertyDescription(docElement)
    }

    return null
  }

  private fun createResourceDescription(element: PsiElement): String {
    val property = element.parent as? JsonProperty
    if (property == null || property.name != "Type") {
      return ""
    }

    val propertyText = (if (property.value != null) property.value!!.text else "").replace("\"", "")

    val resourceType = CloudFormationMetadataProvider.METADATA.findResourceType(propertyText)

    return resourceType?.description ?: ""
  }

  private fun createPropertyDescription(element: PsiElement): String {
    val propertyName = element as? JsonStringLiteral ?: return ""

    val property = propertyName.parent as? JsonProperty
    if (property == null || property.nameElement !== propertyName) {
      return ""
    }

    val resourceElement = CloudFormationPsiUtils.getResourceElementFromPropertyName(propertyName) ?: return ""

    val resourceValue = resourceElement.value as? JsonObject ?: return ""

    val typeProperty = resourceValue.findProperty(CloudFormationConstants.TypePropertyName) ?: return ""

    val typeValue = typeProperty.value as? JsonStringLiteral ?: return ""

    val type = CloudFormationResolve.getTargetName(typeValue)

    val resourceTypeMetadata = CloudFormationMetadataProvider.METADATA.findResourceType(type) ?: return ""

    val propertyMetadata = resourceTypeMetadata.findProperty(property.name) ?: return ""

    val document = "<p>" + propertyMetadata.description + "</p><br>" +
        "<p><i>Required:</i> " + propertyMetadata.required + "</p>" +
        propertyMetadata.type +
        propertyMetadata.updateRequires

    return prefixLinksWithUserGuideRoot(document)
  }

  override fun getCustomDocumentationElement(editor: Editor, file: PsiFile, contextElement: PsiElement?): PsiElement? {
    if (contextElement == null) return null
    return if (getDocElement(contextElement) != null) contextElement else null
  }

  private fun prefixLinksWithUserGuideRoot(html: String): String {
    return html.replace("href=\"(?!http)".toRegex(), "href=\"http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/")
  }
}