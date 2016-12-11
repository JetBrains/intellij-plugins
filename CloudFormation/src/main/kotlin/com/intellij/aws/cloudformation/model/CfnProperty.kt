package com.intellij.aws.cloudformation.model

import com.intellij.aws.cloudformation.IndentWriter

class CfnProperty(val name: CfnNameNode) : CfnNode() {
  override fun dump(writer: IndentWriter) {
    writer.println("property:")
    writer.indent { name.dump(writer) }
  }
}