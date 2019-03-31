package com.intellij.aws.cloudformation.model

class CfnRootNode(
    val metadataNode: CfnMetadataNode?,
    val transformNode: CfnTransformNode?,
    val parametersNode: CfnParametersNode?,
    val mappingsNode: CfnMappingsNode?,
    val conditionsNode: CfnConditionsNode?,
    val resourcesNode: CfnResourcesNode?,
    val globalsNode: CfnGlobalsNode?,
    val outputsNode: CfnOutputsNode?
) : CfnNode() {
  val transformValues
    get() = transformNode?.transforms?.map { it.value } ?: emptyList()

  companion object {
    fun empty() = CfnRootNode(null, null, null, null, null, null, null, null)
  }
}