package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.model.CfnNamedNode
import com.intellij.aws.cloudformation.model.CfnNode
import com.intellij.aws.cloudformation.model.CfnOutputsNode
import com.intellij.aws.cloudformation.model.CfnResourceNode
import com.intellij.aws.cloudformation.model.CfnResourcesNode
import com.intellij.aws.cloudformation.model.CfnStringValueNode

class CloudFormationInspections private constructor(val parsed: CloudFormationParsedFile) {
  val problems: MutableList<CloudFormationProblem> = mutableListOf()

/*
  private fun addProblem(element: PsiElement, description: String) {
    problems.add(Problem(element, description))
  }
*/

  private fun addProblem(element: CfnNode, description: String) {
    // TODO check psi element mapping not exists
    val psiElement = if (element is CfnNamedNode && element.name != null) {
      parsed.getPsiElement(element.name)
    } else {
      parsed.getPsiElement(element)
    }

    problems.add(CloudFormationProblem(psiElement, description))
  }

  /*private fun addProblemOnNameElement(property: JsonProperty, description: String) {
    addProblem(
        if (property.firstChild != null) property.firstChild else property,
        description)
  }
*/

  // TODO check outputs, mappings, parameters, resources name for correctness

  fun outputs(outputsNode: CfnOutputsNode) {
    if (outputsNode.properties.isEmpty()) {
      addProblem(outputsNode, "Outputs section must declare at least one stack output")
    }

    if (outputsNode.properties.size > CloudFormationMetadataProvider.METADATA.limits.maxOutputs) {
      addProblem(outputsNode, CloudFormationBundle.getString("format.max.outputs.exceeded", CloudFormationMetadataProvider.METADATA.limits.maxOutputs))
    }
  }

  fun resource(resource: CfnResourceNode) {
    val resourceType = resource.type
    if (resourceType == null) {
      addProblem(resource, "Type property is required for resource")
      return
    }

    val resourceTypeValue = resourceType.value

    val typeName = (resourceTypeValue as? CfnStringValueNode)?.value
    if (resourceTypeValue == null || typeName == null || typeName.isEmpty()) {
      addProblem(resource, "Type value is required")
      return
    }

    val isCustomResourceType = isCustomResourceType(typeName)
    val resourceTypeMetadata = CloudFormationMetadataProvider.METADATA.findResourceType(
        if (isCustomResourceType) CloudFormationConstants.CustomResourceType else typeName)

    if (!isCustomResourceType && resourceTypeMetadata == null) {
      addProblem(resourceTypeValue, CloudFormationBundle.getString("format.unknown.type", typeName))
    }

    if (resourceTypeMetadata != null) {
      val propertiesNode = resource.properties
      if (propertiesNode == null) {
        val requiredProperties = resourceTypeMetadata.requiredProperties.joinToString(" ")
        if (requiredProperties.isNotEmpty()) {
          addProblem(resource, CloudFormationBundle.getString("format.required.resource.properties.are.not.set", requiredProperties))
        }
      } else {
        propertiesNode.properties.forEach {
          val propertyName = it.name?.value
          if (propertyName != null &&
              propertyName != CloudFormationConstants.CommentResourcePropertyName &&
              !isCustomResourceType &&
              resourceTypeMetadata.findProperty(propertyName) == null) {
            addProblem(it, CloudFormationBundle.getString("format.unknown.resource.type.property", propertyName))
          }
        }

        val missingProperties = resourceTypeMetadata.requiredProperties.filter {
          required -> propertiesNode.properties.none { required == it.name?.value }
        }.joinToString(separator = " ")

        if (missingProperties.isNotEmpty()) {
          addProblem(propertiesNode, CloudFormationBundle.getString("format.required.resource.properties.are.not.set", missingProperties))
        }
      }
    }
  }

  fun resources(resourcesNode: CfnResourcesNode) {
    if (resourcesNode.resources.isEmpty()) {
      addProblem(resourcesNode, "Resources section should declare at least one resource")
      return
    }

    for (resource in resourcesNode.resources) {
      resource(resource)
    }
  }

  fun root() {
    val root = parsed.root

    root.outputsNode?.let { outputs(it) }

    val resourcesNode = root.resourcesNode
    if (resourcesNode == null) {
      addProblem(root, "Resources section is missing")
    } else {
      resources(resourcesNode)
    }
  }

  fun isCustomResourceType(value: String): Boolean {
    return value == CloudFormationConstants.CustomResourceType || value.startsWith(CloudFormationConstants.CustomResourceTypePrefix)
  }

  companion object {
    fun inspectFile(parsed: CloudFormationParsedFile): List<CloudFormationProblem> {
      val inspections = CloudFormationInspections(parsed)
      inspections.root()
      return inspections.problems
    }
  }
}