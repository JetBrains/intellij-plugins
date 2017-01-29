package com.intellij.aws.cloudformation.model

class CfnRootNode(
    val resourcesNode: CfnResourcesNode?,
    val outputsNode: CfnOutputsNode?,
    val mappingsNode: CfnMappingsNode?
) : CfnNode()