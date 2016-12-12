package com.intellij.aws.cloudformation.model

import com.intellij.aws.cloudformation.IndentWriter

class CfnResourceNode(name: CfnNameNode,
                      val type: CfnResourceTypeNode?,
                      val allTopLevelProperties: Map<String, CfnNamedNode>,
                      val properties: CfnResourcePropertiesNode?) : CfnNamedNode(name) {
  val typeName: String?
    get() = type?.value?.id

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