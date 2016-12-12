package com.intellij.aws.cloudformation.model

import com.intellij.aws.cloudformation.IndentWriter

class CfnResourcePropertyNode(name: CfnNameNode) : CfnNamedNode(name) {
  override fun dump(writer: IndentWriter) {
    writer.println("property:")
    writer.indent { name.dump(writer) }
  }
}