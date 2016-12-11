package com.intellij.aws.cloudformation.model

import com.intellij.aws.cloudformation.IndentWriter

class CfnPropertiesNode(val properties: List<CfnProperty>) : CfnNode() {
  override fun dump(writer: IndentWriter) {
    writer.println("properties:")
    writer.indent { properties.forEach { it.dump(writer) } }
  }
}