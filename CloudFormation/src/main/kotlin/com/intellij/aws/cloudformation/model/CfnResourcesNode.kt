package com.intellij.aws.cloudformation.model

import com.intellij.aws.cloudformation.IndentWriter

class CfnResourcesNode(val resources: List<CfnResourceNode>) : CfnNode() {
  override fun dump(writer: IndentWriter) {
    writer.println("resources:")
    writer.indent { resources.forEach { it.dump(writer) } }
  }
}
