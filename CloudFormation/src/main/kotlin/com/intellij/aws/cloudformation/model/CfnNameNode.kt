package com.intellij.aws.cloudformation.model

import com.intellij.aws.cloudformation.IndentWriter

class CfnNameNode(val id: String) : CfnNode() {
  override fun dump(writer: IndentWriter) = writer.println("$id (name)")
}