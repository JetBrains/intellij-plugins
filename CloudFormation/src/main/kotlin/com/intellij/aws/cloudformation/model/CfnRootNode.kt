package com.intellij.aws.cloudformation.model

class CfnRootNode(
    val parametersNode: CfnParametersNode?,
    val resourcesNode: CfnResourcesNode?,
    val outputsNode: CfnOutputsNode?,
    val mappingsNode: CfnMappingsNode?
) : CfnNode()