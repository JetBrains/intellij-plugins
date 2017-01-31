package com.intellij.aws.cloudformation

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceType.Companion.isCustomResourceType
import com.intellij.aws.cloudformation.model.CfnArrayValueNode
import com.intellij.aws.cloudformation.model.CfnFunctionNode
import com.intellij.aws.cloudformation.model.CfnMappingsNode
import com.intellij.aws.cloudformation.model.CfnMetadataNode
import com.intellij.aws.cloudformation.model.CfnNamedNode
import com.intellij.aws.cloudformation.model.CfnNode
import com.intellij.aws.cloudformation.model.CfnObjectValueNode
import com.intellij.aws.cloudformation.model.CfnOutputsNode
import com.intellij.aws.cloudformation.model.CfnParametersNode
import com.intellij.aws.cloudformation.model.CfnResourceConditionNode
import com.intellij.aws.cloudformation.model.CfnResourceDependsOnNode
import com.intellij.aws.cloudformation.model.CfnResourceNode
import com.intellij.aws.cloudformation.model.CfnResourceTypeNode
import com.intellij.aws.cloudformation.model.CfnResourcesNode
import com.intellij.aws.cloudformation.model.CfnRootNode
import com.intellij.aws.cloudformation.model.CfnScalarValueNode
import com.intellij.aws.cloudformation.model.CfnVisitor
import com.intellij.psi.PsiElement

class CloudFormationInspections private constructor(val parsed: CloudFormationParsedFile): CfnVisitor() {
  val problems: MutableList<CloudFormationProblem> = mutableListOf()
  val references: Multimap<PsiElement, CloudFormationReference> = ArrayListMultimap.create()

  private fun addReference(element: CfnNode, reference: CloudFormationReference) {
    references.put(parsed.getPsiElement(element), reference)
  }

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

  var currentResource: CfnResourceNode? = null

  // TODO check outputs, mappings, parameters, resources name for correctness

  override fun function(function: CfnFunctionNode) {
    val arg0 = function.args.getOrNull(0)
    val arg1 = function.args.getOrNull(1)

    // TODO make it sealed when some time in the future
    when (function.functionId) {
      CloudFormationIntrinsicFunction.Ref ->
        if (function.args.size != 1 || arg0 !is CfnScalarValueNode) {
          addProblem(function, "Reference expects one string argument")
        } else {
          if (!CloudFormationMetadataProvider.METADATA.predefinedParameters.contains(arg0.value)) {
            addReference(arg0, CloudFormationReference(arg0.value, ReferenceType.Ref))
          }
        }

      CloudFormationIntrinsicFunction.Condition ->
        if (function.args.size != 1 || arg0 !is CfnScalarValueNode) {
          addProblem(function, "Condition reference expects one string argument")
        } else {
          addReference(arg0, CloudFormationReference(arg0.value, ReferenceType.Condition))
        }

      CloudFormationIntrinsicFunction.FnBase64 ->
        if (function.args.size != 1) {
          addProblem(function, "Base64 reference expects 1 argument")
        }

      CloudFormationIntrinsicFunction.FnFindInMap -> {
        if (function.args.size != 3) {
          addProblem(function, "FindInMap requires 3 arguments")
        }

        val mappingName = function.args[0]
        val firstLevelKey = function.args[1]
        val secondLevelKey = function.args[2]

        if (mappingName is CfnScalarValueNode) {
          addReference(mappingName, CloudFormationReference(mappingName.value, ReferenceType.Mapping))

          val mapping = CloudFormationResolve.resolveMapping(parsed, mappingName.value)
          if (mapping != null && firstLevelKey is CfnScalarValueNode) {
            // TODO addReference(firstLevelKey, CloudFormationReference(firstLevelKey, ))
          }
        }

        // TODO add keys
      }

      CloudFormationIntrinsicFunction.FnGetAtt -> {
        val resourceName: String?
        val attributeName: String?

        if (function.args.size == 1 && arg0 is CfnScalarValueNode && function.name.value == CloudFormationIntrinsicFunction.FnGetAtt.shortForm) {
          val dotIndex = arg0.value.lastIndexOf('.')
          if (dotIndex < 0) {
            addProblem(function, "GetAttr in short form requires argument in the format logicalNameOfResource.attributeName")
            resourceName = null
            attributeName = null
          } else {
            resourceName = arg0.value.substring(0, dotIndex)
            attributeName = arg0.value.substring(dotIndex + 1)
          }
        } else if (function.args.size == 2 && arg0 is CfnScalarValueNode && function.name.value == CloudFormationIntrinsicFunction.FnGetAtt.id) {
          resourceName = arg0.value
          attributeName = if (arg1 is CfnScalarValueNode) arg1.value else null
        } else {
          addProblem(function, "GetAtt requires two string arguments in full form and one string argument in short form")
          resourceName = null
          attributeName = null
        }

        if (resourceName != null) {
          // TODO Add text range
          addReference(arg0!!, CloudFormationReference(resourceName, ReferenceType.Resource))

          if (attributeName != null) {
            // TODO resolve resource and get resource type
            // CloudFormationMetadataProvider.METADATA.resourceTypes
          }
        }
      }

      CloudFormationIntrinsicFunction.FnGetAZs ->
        // TODO verify string agains known regions
        // TODO possibility for dataflow checks
        if (function.args.size != 1) {
          addProblem(function, "GetAZs expects one argument")
        }

      CloudFormationIntrinsicFunction.FnImportValue ->
        if (function.args.size != 1) {
          addProblem(function, "ImportValue expects one argument")
        }

      CloudFormationIntrinsicFunction.FnJoin ->
        if (function.args.size != 2 || arg0 !is CfnScalarValueNode || arg1 !is CfnArrayValueNode) {
          addProblem(function, "Join expects a string argument and an array argument")
        }

      CloudFormationIntrinsicFunction.FnSelect ->
        if (function.args.size != 2) {
          addProblem(function, "Select expects an index argument and an array argument")
        } else if (arg0 is CfnScalarValueNode) {
          try {
            Integer.parseUnsignedInt(arg0.value)
          } catch (t : NumberFormatException) {
            addProblem(function, "Select index should be a valid non-negative number")
          }
        }

      CloudFormationIntrinsicFunction.FnSub -> {
        // TODO Add references to substituted values in a string
        // TODO Add references to the mapping
        if (function.args.size != 1 && !(function.args.size == 2 && arg1 is CfnObjectValueNode)) {
          addProblem(function, "Sub expects one argument plus an optional value map")
        }
      }

      // TODO Check context, valid only in boolean context
      CloudFormationIntrinsicFunction.FnAnd, CloudFormationIntrinsicFunction.FnOr ->
        if (function.args.size < 2) {
          addProblem(function, function.functionId.shortForm + " expects at least 2 arguments")
        }

      CloudFormationIntrinsicFunction.FnEquals ->
        if (function.args.size != 2) {
          addProblem(function, "Equals expects exactly 2 arguments")
        }

      CloudFormationIntrinsicFunction.FnIf ->
        if (function.args.size == 3) {
          if (arg0 is CfnScalarValueNode) {
            addReference(arg0, CloudFormationReference(arg0.value, ReferenceType.Condition))
          } else {
            addProblem(function, "If's first argument should be a condition name")
          }
        } else {
          addProblem(function, "If expects exactly 3 arguments")
        }

      CloudFormationIntrinsicFunction.FnNot ->
        if (function.args.size != 1) {
          addProblem(function, "Not expects exactly 1 argument")
        }
    }

    super.function(function)
  }

  override fun resourceDependsOn(resourceDependsOn: CfnResourceDependsOnNode) {
    resourceDependsOn.dependsOn.forEach { depend ->
      val excludeFromCompletion = mutableListOf<String>()

      currentResource!!.name?.value?.let { excludeFromCompletion.add(it) }
      resourceDependsOn.dependsOn.forEach { if (depend.value != it.value) excludeFromCompletion.add(it.value) }

      addReference(depend, CloudFormationReference(
          depend.value,
          ReferenceType.Resource,
          excludeFromCompletion = excludeFromCompletion))
    }

    super.resourceDependsOn(resourceDependsOn)
  }

  override fun resourceCondition(resourceCondition: CfnResourceConditionNode) {
    resourceCondition.condition?.let {
      addReference(it, CloudFormationReference(it.value, ReferenceType.Condition))
    }

    super.resourceCondition(resourceCondition)
  }

  override fun resourceType(resourceType: CfnResourceTypeNode) {
    val resourceTypeValue = resourceType.value
    val typeName = resourceTypeValue?.value

    if (resourceTypeValue == null || typeName == null || typeName.isEmpty()) {
      addProblem(resourceType, "Type value is required")
      return
    }

    if (!isCustomResourceType(typeName)) {
      val resourceTypeMetadata = CloudFormationMetadataProvider.METADATA.findResourceType(typeName)
      if (resourceTypeMetadata == null) {
        addProblem(resourceTypeValue, CloudFormationBundle.getString("format.unknown.type", typeName))
      }
    }

    super.resourceType(resourceType)
  }

  override fun outputs(outputs: CfnOutputsNode) {
    if (outputs.properties.isEmpty()) {
      addProblem(outputs, "Outputs section must declare at least one stack output")
    }

    if (outputs.properties.size > CloudFormationMetadataProvider.METADATA.limits.maxOutputs) {
      addProblem(outputs, CloudFormationBundle.getString("format.max.outputs.exceeded", CloudFormationMetadataProvider.METADATA.limits.maxOutputs))
    }

    super.outputs(outputs)
  }

  override fun parameters(parameters: CfnParametersNode) {
    if (parameters.parameters.isEmpty()) {
      addProblem(parameters, "Parameters section must declare at least one parameter")
    }

    if (parameters.parameters.size > CloudFormationMetadataProvider.METADATA.limits.maxParameters) {
      addProblem(parameters, CloudFormationBundle.getString("format.max.parameters.exceeded", CloudFormationMetadataProvider.METADATA.limits.maxParameters))
    }

    super.parameters(parameters)
  }

  override fun mappings(mappings: CfnMappingsNode) {
    if (mappings.mappings.isEmpty()) {
      addProblem(mappings, "Mappings section must declare at least one parameter")
    }

    if (mappings.mappings.size > CloudFormationMetadataProvider.METADATA.limits.maxMappings) {
      addProblem(mappings, CloudFormationBundle.getString("format.max.mappings.exceeded", CloudFormationMetadataProvider.METADATA.limits.maxMappings))
    }

    super.mappings(mappings)
  }

  override fun resource(resource: CfnResourceNode) {
    currentResource = resource

    val resourceType = resource.type
    if (resourceType == null) {
      addProblem(resource, "Type property is required for resource")
      return
    }

    val metadata = resourceType.metadata()
    if (metadata != null) {
      val propertiesNode = resource.properties
      if (propertiesNode == null) {
        val requiredProperties = metadata.requiredProperties.joinToString(" ")
        if (requiredProperties.isNotEmpty()) {
          addProblem(resource, CloudFormationBundle.getString("format.required.resource.properties.are.not.set", requiredProperties))
        }
      } else {
        propertiesNode.properties.forEach {
          val propertyName = it.name?.value
          if (propertyName != null &&
              propertyName != CloudFormationConstants.CommentResourcePropertyName &&
              !isCustomResourceType(resourceType.value!!.value) &&
              metadata.findProperty(propertyName) == null) {
            addProblem(it, CloudFormationBundle.getString("format.unknown.resource.type.property", propertyName))
          }
        }

        val missingProperties = metadata.requiredProperties.filter {
          required -> propertiesNode.properties.none { required == it.name?.value }
        }.joinToString(separator = " ")

        if (missingProperties.isNotEmpty()) {
          addProblem(propertiesNode, CloudFormationBundle.getString("format.required.resource.properties.are.not.set", missingProperties))
        }
      }
    }

    super.resource(resource)

    currentResource = null
  }

  override fun resources(resources: CfnResourcesNode) {
    if (resources.resources.isEmpty()) {
      addProblem(resources, "Resources section should declare at least one resource")
      return
    }

    super.resources(resources)
  }

  override fun metadata(metadata: CfnMetadataNode) {
    val cfnInterface = metadata.value?.properties?.singleOrNull {
      it.value is CfnObjectValueNode && it.name?.value == CloudFormationConstants.CloudFormationInterfaceType
    }?.let { it.value as CfnObjectValueNode }

    if (cfnInterface != null) {
      val parameterGroups = cfnInterface.properties
          .singleOrNull { it.name?.value == CloudFormationConstants.CloudFormationInterfaceParameterGroups }
          ?.let { it.value as? CfnArrayValueNode }

      val predefinedParameters = CloudFormationMetadataProvider.METADATA.predefinedParameters

      @Suppress("LoopToCallChain")
      for (parameterGroup in parameterGroups?.items?.mapNotNull { it as? CfnObjectValueNode } ?: emptyList()) {
        val parameters = parameterGroup.properties
            .singleOrNull { it.name?.value == CloudFormationConstants.CloudFormationInterfaceParameters }
            ?.let { it.value as? CfnArrayValueNode }

        for (parameter in parameters?.items ?: emptyList()) {
          if (parameter is CfnScalarValueNode) {
            addReference(parameter, CloudFormationReference(parameter.value, ReferenceType.Parameter, excludeFromCompletion = predefinedParameters))
          } else {
            addProblem(parameter, "Expected a string")
          }
        }
      }

      val parameterLabels = cfnInterface.properties
          .singleOrNull { it.name?.value == CloudFormationConstants.CloudFormationInterfaceParameterLabels }
          ?.let { it.value as? CfnObjectValueNode }
      for (parameterName in parameterLabels?.properties?.mapNotNull { it.name } ?: emptyList()) {
        addReference(parameterName, CloudFormationReference(parameterName.value, ReferenceType.Parameter, excludeFromCompletion = predefinedParameters))
      }
    }

    super.metadata(metadata)
  }

  override fun root(root: CfnRootNode) {
    if (root.resourcesNode == null) {
      addProblem(root, "Resources section is missing")
    }

    super.root(root)
  }

  class InspectionResult(val problems: List<CloudFormationProblem>, val references: Multimap<PsiElement, CloudFormationReference>)

  companion object {
    fun inspectFile(parsed: CloudFormationParsedFile): InspectionResult {
      val inspections = CloudFormationInspections(parsed)
      inspections.root(parsed.root)
      return InspectionResult(inspections.problems, inspections.references)
    }
  }
}
