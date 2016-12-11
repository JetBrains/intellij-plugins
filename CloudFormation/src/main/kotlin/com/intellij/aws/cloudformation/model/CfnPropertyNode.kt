package com.intellij.aws.cloudformation.model

import com.intellij.aws.cloudformation.IndentWriter

class CfnPropertyNode(val name: CfnNameNode) : CfnNode() {
  override fun dump(writer: IndentWriter) {
    writer.println("property:")
    writer.indent { name.dump(writer) }
  }
}