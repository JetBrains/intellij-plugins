package com.intellij.aws.cloudformation.model

import com.intellij.aws.cloudformation.IndentWriter

abstract class CfnExpressionNode() : CfnNode()

class CfnMissingOrInvalidValueNode() : CfnExpressionNode() {
  override fun dump(writer: IndentWriter) = writer.println("(missing value)")
}

class CfnNumberValueNode(val value: String) : CfnExpressionNode() {
  override fun dump(writer: IndentWriter) = writer.println("$value (number)")
}

class CfnBooleanValueNode(val value: Boolean) : CfnExpressionNode() {
  override fun dump(writer: IndentWriter) = writer.println("$value (bool)")
}

class CfnStringValueNode(val value: String) : CfnExpressionNode() {
  override fun dump(writer: IndentWriter) = writer.println("$value (string)")
}

class CfnArrayValueNode(val items: List<CfnExpressionNode>) : CfnExpressionNode() {
  override fun dump(writer: IndentWriter) {
    writer.println("(array)")
    writer.indent {
      items.forEach {
        writer.print("- ")
        it.dump(writer)
      }
    }
  }
}

class CfnObjectValueNode(val properties: List<Pair<CfnStringValueNode, CfnExpressionNode>>) : CfnExpressionNode() {
  override fun dump(writer: IndentWriter) {
    writer.println("(object)")
    writer.indent {
      properties.forEach {
        writer.print("- name: ")
        it.first.dump(writer)
        writer.print("  value: ")
        it.second.dump(writer)
      }
    }
  }
}

class CfnFunctionNode(val name: CfnStringValueNode, val args: List<CfnExpressionNode>) : CfnExpressionNode() {
  override fun dump(writer: IndentWriter) {
    writer.println("$name (function)")
    writer.indent {
      args.forEach {
        writer.print("- ")
        it.dump(writer)
      }
    }
  }
}
