package com.intellij.aws.cloudformation.model

@Suppress("unused", "UNUSED_PARAMETER")
abstract class CfnVisitor {
  open fun root(root: CfnRootNode) {
    root.conditionsNode?.let { conditions(it) }
    root.mappingsNode?.let { mappings(it) }
    root.metadataNode?.let { metadata(it) }
    root.outputsNode?.let { outputs(it) }
    root.parametersNode?.let { parameters(it) }
    root.resourcesNode?.let { resources(it) }
  }

  open fun parameters(parameters: CfnParametersNode) {
    parameters.parameters.forEach { parameter(it) }
  }

  open fun parameter(parameter: CfnParameterNode) {}

  open fun resources(resources: CfnResourcesNode) {
    resources.resources.forEach { resource(it) }
  }

  open fun resource(resource: CfnResourceNode) {
    resource.type?.let { resourceType(it) }
    resource.properties?.let { resourceProperties(it) }
    resource.condition?.let { resourceCondition(it) }
    resource.dependsOn?.let { resourceDependsOn(it) }
  }

  open fun resourceDependsOn(resourceDependsOn: CfnResourceDependsOnNode) {}
  open fun resourceCondition(resourceCondition: CfnResourceConditionNode) {}

  open fun resourceProperties(resourceProperties: CfnResourcePropertiesNode) {
    resourceProperties.properties.forEach { resourceProperty(it) }
  }

  open fun resourceProperty(resourceProperty: CfnResourcePropertyNode) {
    resourceProperty.value?.let { expression(it) }
  }

  open fun resourceType(resourceType: CfnResourceTypeNode) {}

  open fun outputs(outputs: CfnOutputsNode) {
    for (outputNode in outputs.properties) {
      output(outputNode)
    }
  }

  open fun output(output: CfnOutputNode) {
    output.value?.let { expression(output.value) }
  }

  open fun metadata(metadata: CfnMetadataNode) {}
  open fun mappings(mappings: CfnMappingsNode) {}

  open fun conditions(conditions: CfnConditionsNode) {
    conditions.conditions.forEach { condition(it) }
  }

  open fun condition(condition: CfnConditionNode) {
    condition.value?.let { expression(it) }
  }

  open fun expression(expr: CfnExpressionNode) {
    when (expr) {
      is CfnFunctionNode -> function(expr)
      is CfnArrayValueNode -> expr.items.forEach { expression(it) }
      is CfnObjectValueNode -> expr.properties.forEach { it.value?.let { expression(it) } }
    }
  }

  open fun function(function: CfnFunctionNode) {
    function.args.forEach { it?.let { expression(it) } }
  }
}