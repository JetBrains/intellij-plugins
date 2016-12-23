package com.intellij.aws.cloudformation.model

import com.intellij.aws.cloudformation.IndentWriter

class CfnResourcePropertyNode(name: CfnStringValueNode, val value: CfnExpressionNode) : CfnNamedNode(name) {
  override fun dump(writer: IndentWriter) {
    writer.println("property:")

    writer.indent {
      writer.print("- name: ")
      name.dump(writer)
      writer.print("- value: ")
      value.dump(writer)
    }
  }
}