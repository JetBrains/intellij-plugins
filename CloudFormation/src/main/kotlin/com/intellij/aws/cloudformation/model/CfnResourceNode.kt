package com.intellij.aws.cloudformation.model

import com.intellij.aws.cloudformation.IndentWriter

class CfnResourceNode(val name: CfnNameNode,
                      val type: CfnNameNode?,
                      val properties: CfnPropertiesNode?) : CfnNode() {
  override fun dump(writer: IndentWriter) {
    writer.println("resource:")
    writer.indent {
      name.dump(writer)

      if (type != null) {
        writer.print("type: ")
        type.dump(writer)
      }

      properties?.dump(writer)
    }
  }
}