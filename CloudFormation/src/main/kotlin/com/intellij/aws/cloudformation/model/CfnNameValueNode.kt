package com.intellij.aws.cloudformation.model

import com.intellij.aws.cloudformation.IndentWriter

open class CfnNameValueNode(name: CfnNameNode, val value: CfnNameNode) : CfnNamedNode(name) {
  override fun dump(writer: IndentWriter) {
    name.dump(writer)
    writer.indent {
      writer.print("value: ")
      value.dump(writer)
    }
  }
}
