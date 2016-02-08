package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.references.CloudFormationEntityReference
import com.intellij.aws.cloudformation.references.CloudFormationMappingSecondLevelKeyReference
import com.intellij.aws.cloudformation.references.CloudFormationMappingTopLevelKeyReference
import com.intellij.json.psi.*
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import java.util.*

class CloudFormationReferenceProvider : PsiReferenceProvider() {

  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
    if (!CloudFormationPsiUtils.isCloudFormationFile(element)) {
      return PsiReference.EMPTY_ARRAY
    }

    val references = buildFromElement(element)
    return if (!references.isEmpty()) references.toTypedArray() else PsiReference.EMPTY_ARRAY
  }

  companion object {
    val ParametersAndResourcesSections = Arrays.asList(CloudFormationSections.Parameters, CloudFormationSections.Resources)

    fun buildFromElement(element: PsiElement): List<PsiReference> {
      val stringLiteral = element as? JsonStringLiteral ?: return emptyList()

      val result = ArrayList<PsiReference>()

      if (handleRef(stringLiteral, result)) {
        return result
      }

      if (isInCondition(stringLiteral)) {
        result.add(CloudFormationEntityReference(stringLiteral, CloudFormationSections.ConditionsSingletonList, null))
        return result
      }

      val parametersArray = element.parent as? JsonArray
      if (parametersArray != null) {
        val funcProperty = parametersArray.parent as? JsonProperty
        val isFindInMap = funcProperty != null && CloudFormationIntrinsicFunctions.FnFindInMap == funcProperty.name
        val isGetAtt = funcProperty != null && CloudFormationIntrinsicFunctions.FnGetAtt == funcProperty.name
        val isIf = funcProperty != null && CloudFormationIntrinsicFunctions.FnIf == funcProperty.name

        if (isGetAtt || isFindInMap || isIf) {
          val obj = funcProperty!!.parent as? JsonObject
          if (obj != null) {
            val allParameters = parametersArray.valueList
            if (allParameters.size > 0 && element === allParameters[0]) {
              val properties = obj.propertyList
              if (properties.size == 1) {
                if (isGetAtt) {
                  result.add(CloudFormationEntityReference(stringLiteral, CloudFormationSections.ResourcesSingletonList, null))
                  return result
                }

                if (isFindInMap) {
                  result.add(CloudFormationEntityReference(stringLiteral, CloudFormationSections.MappingsSingletonList, null))
                  return result
                }

                if (isIf) {
                  result.add(CloudFormationEntityReference(stringLiteral, CloudFormationSections.ConditionsSingletonList, null))
                  return result
                }
              }
            } else if (allParameters.size > 1 && element === allParameters[1]) {
              if (isFindInMap) {
                val mappingNameExpression = allParameters[0] as? JsonStringLiteral
                if (mappingNameExpression != null) {
                  result.add(CloudFormationMappingTopLevelKeyReference(stringLiteral,
                      CloudFormationResolve.getTargetName(mappingNameExpression)))
                  return result
                }
              }
            } else if (allParameters.size > 2 && element === allParameters[2]) {
              if (isFindInMap) {
                val mappingNameExpression = allParameters[0] as? JsonStringLiteral
                val topLevelKeyExpression = allParameters[1] as? JsonStringLiteral
                if (mappingNameExpression != null && topLevelKeyExpression != null) {
                  result.add(CloudFormationMappingSecondLevelKeyReference(
                      stringLiteral,
                      CloudFormationResolve.getTargetName(mappingNameExpression),
                      CloudFormationResolve.getTargetName(topLevelKeyExpression)))
                  return result
                }
              }
            }
          }
        }
      }

      if (handleDependsOnSingle(stringLiteral, result)) {
        return result
      }

      if (handleDependsOnMultiple(stringLiteral, result)) {
        return result
      }

      if (isInConditionOnResource(element)) {
        result.add(CloudFormationEntityReference(stringLiteral, CloudFormationSections.ConditionsSingletonList, null))
        return result
      }

      return result
    }

    fun handleRef(element: JsonStringLiteral, result: MutableList<PsiReference>): Boolean {
      val refProperty = element.parent as? JsonProperty
      if (refProperty == null || CloudFormationIntrinsicFunctions.Ref != refProperty.name) {
        return false
      }

      if (refProperty.nameElement === element) {
        return false
      }

      val obj = refProperty.parent as? JsonObject ?: return false

      val properties = obj.propertyList
      if (properties.size != 1) {
        return false
      }

      val targetName = CloudFormationResolve.getTargetName(element)
      if (CloudFormationMetadataProvider.METADATA.predefinedParameters.contains(targetName)) {
        return false
      }

      result.add(CloudFormationEntityReference(element, ParametersAndResourcesSections, null))
      return true
    }

    fun isInCondition(element: JsonLiteral): Boolean {
      val conditionProperty = element.parent as? JsonProperty
      if (conditionProperty == null || CloudFormationConstants.ConditionPropertyName != conditionProperty.name) {
        return false
      }

      if (conditionProperty.nameElement === element) {
        return false
      }

      val obj = conditionProperty.parent as? JsonObject
      return obj != null && obj.propertyList.size == 1
    }

    fun handleDependsOnSingle(element: JsonLiteral, result: MutableList<PsiReference>): Boolean {
      val dependsOnProperty = element.parent as? JsonProperty
      if (dependsOnProperty == null || CloudFormationConstants.DependsOnPropertyName != dependsOnProperty.name) {
        return false
      }

      if (dependsOnProperty.nameElement === element) {
        return false
      }

      val resourceProperties = dependsOnProperty.parent as? JsonObject ?: return false

      val resource = resourceProperties.parent as? JsonProperty
      if (resource == null || !isResourceElement(resource)) {
        return false
      }

      result.add(CloudFormationEntityReference(
          element, CloudFormationSections.ResourcesSingletonList, listOf(resource.name)))
      return true
    }

    fun isInConditionOnResource(element: PsiElement): Boolean {
      val conditionProperty = element.parent as? JsonProperty
      if (conditionProperty == null || CloudFormationConstants.ConditionPropertyName != conditionProperty.name) {
        return false
      }

      if (conditionProperty.nameElement === element) {
        return false
      }

      val resourceProperties = conditionProperty.parent as? JsonObject ?: return false

      val resource = resourceProperties.parent as? JsonProperty
      return resource != null && isResourceElement(resource)
    }

    fun handleDependsOnMultiple(element: JsonLiteral, result: MutableList<PsiReference>): Boolean {
      val refArray = element.parent as? JsonArray ?: return false

      val dependsOnProperty = refArray.parent as? JsonProperty
      if (dependsOnProperty == null || CloudFormationConstants.DependsOnPropertyName != dependsOnProperty.name) {
        return false
      }

      val resourceProperties = dependsOnProperty.parent as? JsonObject ?: return false

      val resource = resourceProperties.parent as? JsonProperty
      if (resource == null || !isResourceElement(resource)) {
        return false
      }

      val excludes = HashSet<String>()
      for (childExpression in refArray.valueList) {
        if (childExpression === element) {
          continue
        }

        if (childExpression is JsonLiteral) {
          excludes.add(StringUtil.unquoteString(StringUtil.notNullize(childExpression.getText())))
        }
      }

      excludes.add(resource.name)

      result.add(CloudFormationEntityReference(element, CloudFormationSections.ResourcesSingletonList, excludes))
      return true
    }

    private fun isResourceElement(element: JsonProperty): Boolean {
      val resourcesProperties = element.parent as? JsonObject ?: return false

      val resourcesProperty = resourcesProperties.parent as? JsonProperty
      if (resourcesProperty == null || CloudFormationSections.Resources != resourcesProperty.name) {
        return false
      }

      return CloudFormationPsiUtils.getRootExpression(resourcesProperty.containingFile) === resourcesProperty.parent
    }
  }
}
