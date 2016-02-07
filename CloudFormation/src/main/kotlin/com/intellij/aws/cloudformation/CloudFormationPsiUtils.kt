package com.intellij.aws.cloudformation

import com.intellij.json.psi.JsonLiteral
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.ObjectUtils

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

    return ObjectUtils.tryCast(property.value, JsonObject::class.java)
  }

  fun isResourceTypeValuePosition(position: PsiElement): Boolean {
    val valueExpression = ObjectUtils.tryCast(position, JsonLiteral::class.java) ?: return false

    val typeProperty = ObjectUtils.tryCast(valueExpression.parent, JsonProperty::class.java)
    if (typeProperty == null ||
        typeProperty.value !== valueExpression ||
        CloudFormationConstants.TypePropertyName != typeProperty.name) {
      return false
    }

    val resourceExpression = ObjectUtils.tryCast(typeProperty.parent, JsonObject::class.java) ?: return false

    val resourceProperty = ObjectUtils.tryCast(resourceExpression.parent, JsonProperty::class.java) ?: return false

    val resourcesExpression = ObjectUtils.tryCast(resourceProperty.parent, JsonObject::class.java) ?: return false

    val resourcesProperty = ObjectUtils.tryCast(resourcesExpression.parent, JsonProperty::class.java)
    if (resourcesProperty == null || CloudFormationSections.Resources != StringUtil.stripQuotesAroundValue(resourcesProperty.name)) {
      return false
    }

    val root = CloudFormationPsiUtils.getRootExpression(resourceProperty.containingFile)
    return root === resourcesProperty.parent
  }

  fun isResourcePropertyNamePosition(position: PsiElement): Boolean {
    val resourceProperty = getResourceElementFromPropertyName(position) ?: return false

    val resourcesExpression = ObjectUtils.tryCast(resourceProperty.parent, JsonObject::class.java) ?: return false

    val resourcesProperty = ObjectUtils.tryCast(resourcesExpression.parent, JsonProperty::class.java)
    if (resourcesProperty == null ||
        resourcesProperty.name.isEmpty() ||
        CloudFormationSections.Resources != StringUtil.stripQuotesAroundValue(resourcesProperty.name)) {
      return false
    }

    val root = CloudFormationPsiUtils.getRootExpression(resourceProperty.containingFile)
    return root === resourcesProperty.parent
  }

  fun getResourceElementFromPropertyName(position: PsiElement): JsonProperty? {
    val propertyName = ObjectUtils.tryCast(position, JsonStringLiteral::class.java) ?: return null

    val property = ObjectUtils.tryCast(propertyName.parent, JsonProperty::class.java)
    if (property == null || property.nameElement !== propertyName) {
      return null
    }

    val propertiesExpression = ObjectUtils.tryCast(property.parent, JsonObject::class.java) ?: return null

    val properties = ObjectUtils.tryCast(propertiesExpression.parent, JsonProperty::class.java)
    if (properties == null ||
        properties.value !== propertiesExpression ||
        CloudFormationConstants.PropertiesPropertyName != properties.name) {
      return null
    }

    val resourceExpression = ObjectUtils.tryCast(properties.parent, JsonObject::class.java) ?: return null

    return ObjectUtils.tryCast(resourceExpression.parent, JsonProperty::class.java)
  }
}
