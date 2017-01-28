package com.intellij.aws.cloudformation

import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

object CloudFormationResolve {
  fun getSectionNode(file: PsiFile, name: String): JsonObject? =
      CloudFormationPsiUtils.getObjectLiteralExpressionChild(CloudFormationPsiUtils.getRootExpression(file), name)

  fun getTargetName(element: JsonStringLiteral): String = StringUtil.unquoteString(element.text ?: "")

  fun resolveEntity(file: PsiFile, entityName: String, sections: Collection<CloudFormationSection>): JsonProperty? {
    return sections
        .map { getSectionNode(file, it.id) }
        .filterNotNull()
        .map { it.findProperty(entityName) }
        .filterNotNull()
        .firstOrNull()
  }

  fun getEntities(file: PsiFile, sections: Collection<String>): Set<String> =
      sections.flatMap { sectionName ->
        getSectionNode(file, sectionName)?.propertyList?.map { it.name } ?: emptyList()
      }.toSet()

  fun resolveTopLevelMappingKey(file: PsiFile, mappingName: String, topLevelKey: String): JsonProperty? {
    val mappingExpression = resolveEntity(file, mappingName, CloudFormationSection.MappingsSingletonList)?.value as? JsonObject
    return mappingExpression?.findProperty(topLevelKey)
  }

  fun resolveSecondLevelMappingKey(file: PsiFile, mappingName: String, topLevelKey: String, secondLevelKey: String): PsiElement? {
    val topLevelKeyExpression = resolveTopLevelMappingKey(file, mappingName, topLevelKey)?.value as? JsonObject
    return topLevelKeyExpression?.findProperty(secondLevelKey)
  }

  fun getTopLevelMappingKeys(file: PsiFile, mappingName: String): List<String>? {
    val mappingElement = resolveEntity(file, mappingName, CloudFormationSection.MappingsSingletonList)
    val objectLiteralExpression = mappingElement?.value as? JsonObject
    return getPropertiesName(objectLiteralExpression?.propertyList)
  }

  fun getSecondLevelMappingKeys(file: PsiFile, mappingName: String, topLevelKey: String): List<String>? {
    val topLevelKeyElement = resolveTopLevelMappingKey(file, mappingName, topLevelKey)
    val objectLiteralExpression = topLevelKeyElement?.value as? JsonObject
    return getPropertiesName(objectLiteralExpression?.propertyList)
  }

  private fun getPropertiesName(properties: MutableList<JsonProperty>?): List<String>? =
      properties?.map { it.name }
}
