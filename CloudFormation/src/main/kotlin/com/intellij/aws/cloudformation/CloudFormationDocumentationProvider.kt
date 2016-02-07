package com.intellij.aws.cloudformation

import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.ObjectUtils

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
    val property = ObjectUtils.tryCast(element.parent, JsonProperty::class.java)
    if (property == null || property.name != "Type") {
      return ""
    }

    for (resourceType in CloudFormationMetadataProvider.METADATA.resourceTypes) {
      val propertyText = if (property.value != null) property.value!!.text else ""

      if (resourceType.name == propertyText.replace("\"", ""))
        return prefixLinksWithUserGuideRoot(resourceType.description)
    }

    return ""
  }

  private fun createPropertyDescription(element: PsiElement): String {
    val propertyName = ObjectUtils.tryCast(element, JsonStringLiteral::class.java) ?: return ""

    val property = ObjectUtils.tryCast(propertyName.parent, JsonProperty::class.java)
    if (property == null || property.nameElement !== propertyName) {
      return ""
    }

    val resourceElement = CloudFormationPsiUtils.getResourceElementFromPropertyName(propertyName) ?: return ""

    val resourceValue = ObjectUtils.tryCast(resourceElement.value, JsonObject::class.java) ?: return ""

    val typeProperty = resourceValue.findProperty(CloudFormationConstants.TypePropertyName) ?: return ""

    val typeValue = ObjectUtils.tryCast(typeProperty.value, JsonStringLiteral::class.java) ?: return ""

    val type = CloudFormationResolve.getTargetName(typeValue)

    val resourceTypeMetadata = CloudFormationMetadataProvider.METADATA.findResourceType(type) ?: return ""

    for (propertyMetadata in resourceTypeMetadata.properties) {
      if (propertyMetadata.name == property.name) {
        val document = "<p>" + propertyMetadata.description + "</p><br>" +
            "<p><i>Required:</i> " + propertyMetadata.required + "</p>" +
            propertyMetadata.type +
            propertyMetadata.updateRequires

        return prefixLinksWithUserGuideRoot(document)
      }

    }
    return ""
  }

  override fun getCustomDocumentationElement(editor: Editor, file: PsiFile, contextElement: PsiElement?): PsiElement? {
    if (contextElement == null) return null
    return if (getDocElement(contextElement) != null) contextElement else null
  }

  private fun prefixLinksWithUserGuideRoot(html: String): String {
    return html.replace("href=\"(?!http)".toRegex(), "href=\"http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/")
  }
}