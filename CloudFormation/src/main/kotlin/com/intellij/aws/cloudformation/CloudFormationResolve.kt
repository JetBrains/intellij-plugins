package com.intellij.aws.cloudformation

import com.intellij.psi.PsiFile
import com.intellij.psi.PsiElement
import com.intellij.openapi.util.text.StringUtil
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.json.psi.JsonProperty

public open class CloudFormationResolve() {
  companion object {
    public open fun getSectionNode(file: PsiFile, name: String): JsonObject? =
        CloudFormationPsiUtils.getObjectLiteralExpressionChild(CloudFormationPsiUtils.getRootExpression(file), name)

    public open fun getTargetName(element: JsonStringLiteral): String = StringUtil.unquoteString(element.text ?: "")

    public open fun resolveEntity(file: PsiFile, entityName: String, sections: Collection<String>): JsonProperty? {
      return sections
          .map { getSectionNode(file, it) }
          .filterNotNull()
          .map { it.findProperty(entityName) }
          .filterNotNull()
          .firstOrNull()
    }

    public open fun getEntities(file: PsiFile, sections: Collection<String>): Set<String> =
      sections.flatMap { sectionName ->
        getSectionNode(file, sectionName)?.propertyList?.map { it.name } ?: emptyList()
      }.toSet()

    public open fun resolveTopLevelMappingKey(file: PsiFile, mappingName: String, topLevelKey: String): JsonProperty? {
      val mappingExpression = resolveEntity(file, mappingName, CloudFormationSections.MappingsSingletonList)?.value as? JsonObject
      return mappingExpression?.findProperty(topLevelKey)
    }

    public open fun resolveSecondLevelMappingKey(file: PsiFile, mappingName: String, topLevelKey: String, secondLevelKey: String): PsiElement? {
      val topLevelKeyExpression = resolveTopLevelMappingKey(file, mappingName, topLevelKey)?.value as? JsonObject
      return topLevelKeyExpression?.findProperty(secondLevelKey)
    }

    public open fun getTopLevelMappingKeys(file: PsiFile, mappingName: String): Array<String>? {
      val mappingElement = resolveEntity(file, mappingName, CloudFormationSections.MappingsSingletonList)
      val objectLiteralExpression = mappingElement?.value as? JsonObject
      return getPropertiesName(objectLiteralExpression?.propertyList)
    }

    public open fun getSecondLevelMappingKeys(file: PsiFile, mappingName: String, topLevelKey: String): Array<String>? {
      val topLevelKeyElement = resolveTopLevelMappingKey(file, mappingName, topLevelKey)
      val objectLiteralExpression = topLevelKeyElement?.value as? JsonObject
      return getPropertiesName(objectLiteralExpression?.propertyList)
    }

    private fun getPropertiesName(properties: MutableList<JsonProperty>?): Array<String>? =
        properties?.map { it.name }?.toTypedArray()
  }
}
