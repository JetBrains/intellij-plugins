package com.intellij.aws.cloudformation

import com.intellij.json.psi.JsonLiteral
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

object CloudFormationPsiUtils {
  fun isCloudFormationFile(element: PsiElement): Boolean =
      element.containingFile.fileType === CloudFormationFileType.INSTANCE

  fun getRootExpression(file: PsiFile): JsonObject? {
    var cur: PsiElement? = file.firstChild
    while (cur != null) {
      if (cur is JsonObject) {
        return cur
      }
      cur = cur.nextSibling
    }

    return null
  }

  fun getObjectLiteralExpressionChild(parent: JsonObject?, childName: String): JsonObject? {
    val property = parent?.findProperty(childName) ?: return null
    return property.value as? JsonObject
  }

  fun isResourceTypeValuePosition(position: PsiElement): Boolean {
    val valueExpression = position as? JsonLiteral ?: return false

    val typeProperty = valueExpression.parent as? JsonProperty
    if (typeProperty == null ||
        typeProperty.value !== valueExpression ||
        CloudFormationConstants.TypePropertyName != typeProperty.name) {
      return false
    }

    val resourceExpression = typeProperty.parent as? JsonObject ?: return false

    val resourceProperty = resourceExpression.parent as? JsonProperty ?: return false

    val resourcesExpression = resourceProperty.parent as? JsonObject ?: return false

    val resourcesProperty = resourcesExpression.parent as? JsonProperty
    if (resourcesProperty == null || CloudFormationSections.Resources != StringUtil.stripQuotesAroundValue(resourcesProperty.name)) {
      return false
    }

    val root = CloudFormationPsiUtils.getRootExpression(resourceProperty.containingFile)
    return root === resourcesProperty.parent
  }

  fun isResourcePropertyNamePosition(position: PsiElement): Boolean {
    val resourceProperty = getResourceElementFromPropertyName(position) ?: return false

    val resourcesExpression = resourceProperty.parent as? JsonObject ?: return false

    val resourcesProperty = resourcesExpression.parent as? JsonProperty
    if (resourcesProperty == null ||
        resourcesProperty.name.isEmpty() ||
        CloudFormationSections.Resources != StringUtil.stripQuotesAroundValue(resourcesProperty.name)) {
      return false
    }

    val root = CloudFormationPsiUtils.getRootExpression(resourceProperty.containingFile)
    return root === resourcesProperty.parent
  }

  fun getResourceElementFromPropertyName(position: PsiElement): JsonProperty? {
    val propertyName = position as? JsonStringLiteral ?: return null

    val property = propertyName.parent as? JsonProperty
    if (property == null || property.nameElement !== propertyName) {
      return null
    }

    val propertiesExpression = property.parent as? JsonObject ?: return null

    val properties = propertiesExpression.parent as? JsonProperty
    if (properties == null ||
        properties.value !== propertiesExpression ||
        CloudFormationConstants.PropertiesPropertyName != properties.name) {
      return null
    }

    val resourceExpression = properties.parent as? JsonObject ?: return null
    return resourceExpression.parent as? JsonProperty
  }
}
