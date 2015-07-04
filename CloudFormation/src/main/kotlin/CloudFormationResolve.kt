package com.intellij.aws.cloudformation

import com.intellij.psi.PsiFile
import java.util.HashSet
import com.intellij.util.ObjectUtils
import com.intellij.psi.PsiElement
import com.intellij.util.ArrayUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.json.psi.JsonProperty

public open class CloudFormationResolve() {
  companion object {
    public open fun getSectionNode(file: PsiFile, name: String): JsonObject? =
        CloudFormationPsiUtils.getObjectLiteralExpressionChild(CloudFormationPsiUtils.getRootExpression(file), name)

    public open fun getTargetName(element: JsonStringLiteral): String = StringUtil.stripQuotesAroundValue(element.getText() ?: "")

    public open fun resolveEntity(file: PsiFile, entityName: String, vararg sections: String): JsonProperty? {
      return sections
          .map { getSectionNode(file, it) }
          .filterNotNull()
          .map { it.findProperty(entityName) }
          .filterNotNull()
          .firstOrNull()
    }

    public open fun getEntities(file: PsiFile, sections: Array<String>): Set<String> {
      val result = HashSet<String>()

      for (sectionName in sections)
      {
        val section = getSectionNode(file, sectionName)
        if (section != null)
        {
          for (property in section.getPropertyList())
          {
            val name = property.getName()
            result.add(name)
          }
        }
      }

      return result
    }

    public open fun resolveTopLevelMappingKey(file: PsiFile, mappingName: String, topLevelKey: String): JsonProperty? {
      val mappingExpression = resolveEntity(file, mappingName, CloudFormationSections.Mappings)?.getValue() as? JsonObject
      return mappingExpression?.findProperty(topLevelKey)
    }

    public open fun resolveSecondLevelMappingKey(file: PsiFile, mappingName: String, topLevelKey: String, secondLevelKey: String): PsiElement? {
      val topLevelKeyExpression = resolveTopLevelMappingKey(file, mappingName, topLevelKey)?.getValue() as? JsonObject
      return topLevelKeyExpression?.findProperty(secondLevelKey)
    }

    public open fun getTopLevelMappingKeys(file: PsiFile, mappingName: String): Array<String>? {
      val mappingElement = resolveEntity(file, mappingName, CloudFormationSections.Mappings)
      if (mappingElement == null)
      {
        return null
      }

      val objectLiteralExpression = ObjectUtils.tryCast(mappingElement.getValue(), javaClass<JsonObject>())
      if (objectLiteralExpression == null)
      {
        return null
      }

      return getPropertiesName(objectLiteralExpression.getPropertyList())
    }

    public open fun getSecondLevelMappingKeys(file: PsiFile, mappingName: String, topLevelKey: String): Array<String>? {
      val topLevelKeyElement = resolveTopLevelMappingKey(file, mappingName, topLevelKey)
      if (topLevelKeyElement == null)
      {
        return null
      }

      val objectLiteralExpression = ObjectUtils.tryCast(topLevelKeyElement.getValue(), javaClass<JsonObject>())
      if (objectLiteralExpression == null)
      {
        return null
      }

      return getPropertiesName(objectLiteralExpression.getPropertyList())
    }

    private fun getPropertiesName(properties: MutableList<JsonProperty>?): Array<String>? {
      if (properties == null)
      {
        return null
      }

      val result = HashSet<String>()
      for (property in properties)
      {
        val name = property.getName()
        result.add(name)
      }
      return ArrayUtil.toStringArray(result)
    }
  }
}
