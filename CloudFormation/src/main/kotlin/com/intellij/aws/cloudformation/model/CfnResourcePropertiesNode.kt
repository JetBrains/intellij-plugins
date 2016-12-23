package com.intellij.aws.cloudformation.model

import com.intellij.aws.cloudformation.IndentWriter

class CfnResourcePropertiesNode(name: CfnStringValueNode, val properties: List<CfnResourcePropertyNode>) : CfnNamedNode(name) {
  override fun dump(writer: IndentWriter) {
    writer.print("properties: ")
    name.dump(writer)
    writer.indent { properties.forEach { it.dump(writer) } }
  }
}