package com.intellij.aws.cloudformation.model

class CfnRootNode(
    val metadataNode: CfnMetadataNode?,
    val transformNode: CfnTransformNode?,
    val parametersNode: CfnParametersNode?,
    val mappingsNode: CfnMappingsNode?,
    val conditionsNode: CfnConditionsNode?,
    val resourcesNode: CfnResourcesNode?,
    val outputsNode: CfnOutputsNode?
) : CfnNode() {
  val transformStringValue
    get() = transformNode?.value?.value
}