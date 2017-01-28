package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.references.CloudFormationEntityReference
import com.intellij.aws.cloudformation.references.CloudFormationMappingSecondLevelKeyReference
import com.intellij.aws.cloudformation.references.CloudFormationMappingTopLevelKeyReference
import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonLiteral
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import java.util.HashSet

class CloudFormationReferenceProvider : PsiReferenceProvider() {

  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
    if (!CloudFormationPsiUtils.isCloudFormationFile(element)) {
      return PsiReference.EMPTY_ARRAY
    }

    val reference = buildFromElement(element)
    return if (reference != null) arrayOf(reference) else PsiReference.EMPTY_ARRAY
  }

  companion object {
    val ParametersAndResourcesSections = listOf(CloudFormationSections.Parameters, CloudFormationSections.Resources)

    fun buildFromElement(element: PsiElement): PsiReference? {
      val stringLiteral = element as? JsonStringLiteral ?: return null

      val handleRef = handleRef(stringLiteral)
      if (handleRef != null) {
        return handleRef
      }

      val handleCloudFormationInterfaceParameterLabels = handleCloudFormationInterfaceParameterLabels(stringLiteral)
      if (handleCloudFormationInterfaceParameterLabels != null) {
        return handleCloudFormationInterfaceParameterLabels
      }

      val handleCloudFormationInterfaceParameterGroups = handleCloudFormationInterfaceParameterGroups(stringLiteral)
      if (handleCloudFormationInterfaceParameterGroups != null) {
        return handleCloudFormationInterfaceParameterGroups
      }

      if (isInCondition(stringLiteral)) {
        return CloudFormationEntityReference(stringLiteral, CloudFormationSections.ConditionsSingletonList, null)
      }

      val parametersArray = element.parent as? JsonArray
      if (parametersArray != null) {
        val funcProperty = parametersArray.parent as? JsonProperty
        val isFindInMap = funcProperty != null && CloudFormationIntrinsicFunctions.FnFindInMap.id == funcProperty.name
        val isGetAtt = funcProperty != null && CloudFormationIntrinsicFunctions.FnGetAtt.id == funcProperty.name
        val isIf = funcProperty != null && CloudFormationIntrinsicFunctions.FnIf.id == funcProperty.name

        if (isGetAtt || isFindInMap || isIf) {
          val obj = funcProperty!!.parent as? JsonObject
          if (obj != null) {
            val allParameters = parametersArray.valueList
            if (allParameters.size > 0 && element === allParameters[0]) {
              val properties = obj.propertyList
              if (properties.size == 1) {
                if (isGetAtt) {
                  return CloudFormationEntityReference(stringLiteral, CloudFormationSections.ResourcesSingletonList, null)
                }

                if (isFindInMap) {
                  return CloudFormationEntityReference(stringLiteral, CloudFormationSections.MappingsSingletonList, null)
                }

                if (isIf) {
                  return CloudFormationEntityReference(stringLiteral, CloudFormationSections.ConditionsSingletonList, null)
                }
              }
            } else if (allParameters.size > 1 && element === allParameters[1]) {
              if (isFindInMap) {
                val mappingNameExpression = allParameters[0] as? JsonStringLiteral
                if (mappingNameExpression != null) {
                  return CloudFormationMappingTopLevelKeyReference(stringLiteral, CloudFormationResolve.getTargetName(mappingNameExpression))
                }
              }
            } else if (allParameters.size > 2 && element === allParameters[2]) {
              if (isFindInMap) {
                val mappingNameExpression = allParameters[0] as? JsonStringLiteral
                val topLevelKeyExpression = allParameters[1] as? JsonStringLiteral
                if (mappingNameExpression != null && topLevelKeyExpression != null) {
                  return CloudFormationMappingSecondLevelKeyReference(
                      stringLiteral,
                      CloudFormationResolve.getTargetName(mappingNameExpression),
                      CloudFormationResolve.getTargetName(topLevelKeyExpression))
                }
              }
            }
          }
        }
      }

      val handleDependsOnSingle = handleDependsOnSingle(stringLiteral)
      if (handleDependsOnSingle != null) {
        return handleDependsOnSingle
      }

      val handleDependsOnMultiple = handleDependsOnMultiple(stringLiteral)
      if (handleDependsOnMultiple != null) {
        return handleDependsOnMultiple
      }

      if (isInConditionOnResource(element)) {
        return CloudFormationEntityReference(stringLiteral, CloudFormationSections.ConditionsSingletonList, null)
      }

      return null
    }

    fun handleRef(element: JsonStringLiteral): PsiReference? {
      val refProperty = element.parent as? JsonProperty
      if (refProperty == null || CloudFormationIntrinsicFunctions.Ref.id != refProperty.name) {
        return null
      }

      if (refProperty.nameElement === element) {
        return null
      }

      val obj = refProperty.parent as? JsonObject ?: return null

      val properties = obj.propertyList
      if (properties.size != 1) {
        return null
      }

      val targetName = CloudFormationResolve.getTargetName(element)
      if (CloudFormationMetadataProvider.METADATA.predefinedParameters.contains(targetName)) {
        return null
      }

      return CloudFormationEntityReference(element, ParametersAndResourcesSections, null)
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

    fun handleDependsOnSingle(element: JsonLiteral): PsiReference? {
      val dependsOnProperty = element.parent as? JsonProperty
      if (dependsOnProperty == null || CloudFormationConstants.DependsOnPropertyName != dependsOnProperty.name) {
        return null
      }

      if (dependsOnProperty.nameElement === element) {
        return null
      }

      val resourceProperties = dependsOnProperty.parent as? JsonObject ?: return null

      val resource = resourceProperties.parent as? JsonProperty
      if (resource == null || !isResourceElement(resource)) {
        return null
      }

      return CloudFormationEntityReference(element, CloudFormationSections.ResourcesSingletonList, listOf(resource.name))
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

    fun handleDependsOnMultiple(element: JsonLiteral): PsiReference? {
      val refArray = element.parent as? JsonArray ?: return null

      val dependsOnProperty = refArray.parent as? JsonProperty
      if (dependsOnProperty == null || CloudFormationConstants.DependsOnPropertyName != dependsOnProperty.name) {
        return null
      }

      val resourceProperties = dependsOnProperty.parent as? JsonObject ?: return null

      val resource = resourceProperties.parent as? JsonProperty
      if (resource == null || !isResourceElement(resource)) {
        return null
      }

      val excludes = HashSet<String>()
      @Suppress("LoopToCallChain")
      for (childExpression in refArray.valueList) {
        if (childExpression === element) {
          continue
        }

        if (childExpression is JsonLiteral) {
          excludes.add(StringUtil.unquoteString(StringUtil.notNullize(childExpression.getText())))
        }
      }

      excludes.add(resource.name)

      return CloudFormationEntityReference(element, CloudFormationSections.ResourcesSingletonList, excludes)
    }

    private fun isResourceElement(element: JsonProperty): Boolean {
      val resourcesProperties = element.parent as? JsonObject ?: return false

      val resourcesProperty = resourcesProperties.parent as? JsonProperty
      if (resourcesProperty == null || CloudFormationSections.Resources.id != resourcesProperty.name) {
        return false
      }

      return CloudFormationPsiUtils.getRootExpression(resourcesProperty.containingFile) === resourcesProperty.parent
    }

    private fun handleCloudFormationInterfaceParameterLabels(element: JsonStringLiteral): PsiReference? {
      val labelProperty = element.parent as? JsonProperty
      if (labelProperty?.nameElement != element) return null

      val labelsObject = labelProperty?.parent as? JsonObject
      val parameterLabelsProperty = labelsObject?.parent as? JsonProperty

      if (!isAWSCloudFormationInterfaceProperty(parameterLabelsProperty, CloudFormationConstants.CloudFormationInterfaceParameterLabels)) return null

      return CloudFormationEntityReference(element, CloudFormationSections.ParametersSingletonList, CloudFormationMetadataProvider.METADATA.predefinedParameters)
    }

    private fun handleCloudFormationInterfaceParameterGroups(element: JsonStringLiteral): PsiReference? {
      val parametersArray = element.parent as? JsonArray

      val parametersProperty = parametersArray?.parent as? JsonProperty
      if (parametersProperty?.name != CloudFormationConstants.CloudFormationInterfaceParameters) return null

      val parameterGroup = parametersProperty?.parent as? JsonObject
      val parameterGroups = parameterGroup?.parent as? JsonArray
      val parameterGroupsElement = parameterGroups?.parent as? JsonProperty

      if (!isAWSCloudFormationInterfaceProperty(parameterGroupsElement, CloudFormationConstants.CloudFormationInterfaceParameterGroups)) return null

      return CloudFormationEntityReference(element, CloudFormationSections.ParametersSingletonList, CloudFormationMetadataProvider.METADATA.predefinedParameters)
    }

    private fun isAWSCloudFormationInterfaceProperty(element: JsonProperty?, name: String): Boolean {
      if (element?.name != name) return false

      val interfaceObject = element?.parent as? JsonObject
      val interfaceElement = interfaceObject?.parent as? JsonProperty

      return isAWSCloudFormationInterfaceElement(interfaceElement)
    }

    private fun isAWSCloudFormationInterfaceElement(element: JsonProperty?): Boolean {
      if (element == null || element.name != CloudFormationConstants.CloudFormationInterfaceType) return false

      val metadataSectionObject = element.parent as? JsonObject
      val metadataSectionElement = metadataSectionObject?.parent as? JsonProperty
      if (metadataSectionElement == null || metadataSectionElement.name != CloudFormationSections.Metadata.id) return false

      return CloudFormationPsiUtils.getRootExpression(element.containingFile) === metadataSectionElement.parent
    }
  }
}
