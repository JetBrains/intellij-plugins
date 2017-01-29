package com.intellij.aws.cloudformation.model

class CfnRootNode(
    val metadataNode: CfnMetadataNode?,
    val parametersNode: CfnParametersNode?,
    val mappingsNode: CfnMappingsNode?,
    val conditionsNode: CfnConditionsNode?,
    val resourcesNode: CfnResourcesNode?,
    val outputsNode: CfnOutputsNode?
) : CfnNode()