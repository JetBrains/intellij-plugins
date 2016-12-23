package com.intellij.aws.cloudformation.model

import com.intellij.aws.cloudformation.IndentWriter

open class CfnNameValueNode(name: CfnStringValueNode, val value: CfnStringValueNode) : CfnNamedNode(name) {
  override fun dump(writer: IndentWriter) {
    name.dump(writer)
    writer.indent {
      writer.print("value: ")
      value.dump(writer)
    }
  }
}
