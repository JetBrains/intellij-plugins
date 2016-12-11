package com.intellij.aws.cloudformation.model

import com.intellij.aws.cloudformation.IndentWriter

abstract class CfnNode() {
  abstract fun dump(writer: IndentWriter)
}

class CfnNameNode(val id: String) : CfnNode() {
  override fun dump(writer: IndentWriter) = writer.println("$id (name)")
}

class CfnRootNode(
    val resourcesNode: CfnResourcesNode?
) : CfnNode() {
  override fun dump(writer: IndentWriter) {
    resourcesNode?.dump(writer)
  }
}

class CfnResourcesNode(val resources: List<CfnResourceNode>) : CfnNode() {
  override fun dump(writer: IndentWriter) {
    writer.println("resources:")
    writer.indent { resources.forEach { it.dump(writer) } }
  }
}

class CfnResourceNode(val name: CfnNameNode, val type: CfnNameNode?, val properties: CfnPropertiesNode?) : CfnNode() {
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

class CfnPropertiesNode(val properties: List<CfnProperty>) : CfnNode() {
  override fun dump(writer: IndentWriter) {
    writer.println("properties:")
    writer.indent { properties.forEach { it.dump(writer) } }
  }
}

class CfnProperty(val name: CfnNameNode) : CfnNode() {
  override fun dump(writer: IndentWriter) {
    writer.println("property:")
    writer.indent { name.dump(writer) }
  }
}
