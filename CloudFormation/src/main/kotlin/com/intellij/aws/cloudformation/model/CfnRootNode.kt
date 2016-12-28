package com.intellij.aws.cloudformation.model

import com.intellij.aws.cloudformation.IndentWriter

class CfnRootNode(
    val resourcesNode: CfnResourcesNode?,
    val outputsNode: CfnOutputsNode?
) : CfnNode() {
  override fun dump(writer: IndentWriter) {
    resourcesNode?.dump(writer)
  }
}