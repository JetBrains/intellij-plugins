package com.intellij.aws.cloudformation

import com.intellij.psi.PsiFile
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import java.util.HashSet
import com.intellij.util.ObjectUtils
import com.intellij.psi.PsiElement
import com.intellij.util.ArrayUtil
import com.intellij.openapi.util.text.StringUtil

public open class CloudFormationResolve() {
  class object {
    public open fun getSectionNode(file: PsiFile, name: String): JSObjectLiteralExpression? {
      return CloudFormationPsiUtils.getObjectLiteralExpressionChild(CloudFormationPsiUtils.getRootExpression(file), name)
    }

    public open fun getTargetName(element: JSLiteralExpression): String {
      return StringUtil.stripQuotesAroundValue(StringUtil.notNullize(element.getText()))
    }

    public open fun resolveEntity(file: PsiFile, entityName: String, vararg sections: String): JSProperty? {
      for (sectionName in sections)
      {
        val section = getSectionNode(file, sectionName)
        if (section != null)
        {
          val property = section.findProperty(entityName)
          if (property != null)
          {
            return property
          }

        }

      }
      return null
    }

    public open fun getEntities(file: PsiFile, sections: Array<String>): Set<String> {
      val result = HashSet<String>()
      for (sectionName in sections)
      {
        val section = getSectionNode(file, sectionName)
        if (section != null)
        {
          for (property in section.getProperties()!!)
          {
            val name = property.getName()

            if (name != null) {
              result.add(name)
            }
          }
        }

      }
      return result
    }

    public open fun resolveTopLevelMappingKey(file: PsiFile, mappingName: String, topLevelKey: String): JSProperty? {
      val mappingElement = resolveEntity(file, mappingName, CloudFormationSections.Mappings)
      if (mappingElement == null)
      {
        return null
      }

      val objectLiteralExpression = ObjectUtils.tryCast(mappingElement.getValue(), javaClass<JSObjectLiteralExpression>())
      if (objectLiteralExpression == null)
      {
        return null
      }

      return objectLiteralExpression.findProperty(topLevelKey)
    }

    public open fun resolveSecondLevelMappingKey(file: PsiFile, mappingName: String, topLevelKey: String, secondLevelKey: String): PsiElement? {
      val topLevelKeyElement = resolveTopLevelMappingKey(file, mappingName, topLevelKey)
      if (topLevelKeyElement == null)
      {
        return null
      }

      val objectLiteralExpression = ObjectUtils.tryCast(topLevelKeyElement.getValue(), javaClass<JSObjectLiteralExpression>())
      if (objectLiteralExpression == null)
      {
        return null
      }

      return objectLiteralExpression.findProperty(secondLevelKey)
    }

    public open fun getTopLevelMappingKeys(file: PsiFile, mappingName: String): Array<String>? {
      val mappingElement = resolveEntity(file, mappingName, CloudFormationSections.Mappings)
      if (mappingElement == null)
      {
        return null
      }

      val objectLiteralExpression = ObjectUtils.tryCast(mappingElement.getValue(), javaClass<JSObjectLiteralExpression>())
      if (objectLiteralExpression == null)
      {
        return null
      }

      return getPropertiesName(objectLiteralExpression.getProperties())
    }

    public open fun getSecondLevelMappingKeys(file: PsiFile, mappingName: String, topLevelKey: String): Array<String>? {
      val topLevelKeyElement = resolveTopLevelMappingKey(file, mappingName, topLevelKey)
      if (topLevelKeyElement == null)
      {
        return null
      }

      val objectLiteralExpression = ObjectUtils.tryCast(topLevelKeyElement.getValue(), javaClass<JSObjectLiteralExpression>())
      if (objectLiteralExpression == null)
      {
        return null
      }

      return getPropertiesName(objectLiteralExpression.getProperties())
    }

    private fun getPropertiesName(properties: Array<JSProperty>?): Array<String>? {
      if (properties == null)
      {
        return null
      }

      val result = HashSet<String>()
      for (property in properties)
      {
        val name = property.getName()

        if (name != null) {
          result.add(name)
        }
      }
      return ArrayUtil.toStringArray(result)
    }
  }
}
