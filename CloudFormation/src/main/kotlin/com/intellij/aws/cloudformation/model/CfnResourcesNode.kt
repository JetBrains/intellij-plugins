package com.intellij.aws.cloudformation.model

import com.intellij.aws.cloudformation.IndentWriter

class CfnResourcesNode(name: CfnStringValueNode, val resources: List<CfnResourceNode>) : CfnNamedNode(name) {
  override fun dump(writer: IndentWriter) {
    writer.println("resources:")
    writer.indent { resources.forEach { it.dump(writer) } }
  }
}

class CfnOutputsNode(name: CfnStringValueNode, val properties: List<Pair<CfnStringValueNode, CfnExpressionNode>>) : CfnNamedNode(name) {
  override fun dump(writer: IndentWriter) {
    throw NotImplementedError()
  }
}
