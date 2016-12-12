package com.intellij.aws.cloudformation.model

import com.intellij.aws.cloudformation.IndentWriter

class CfnResourcesNode(name: CfnNameNode, val resources: List<CfnResourceNode>) : CfnNamedNode(name) {
  override fun dump(writer: IndentWriter) {
    writer.println("resources:")
    writer.indent { resources.forEach { it.dump(writer) } }
  }
}
