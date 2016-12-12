package com.intellij.aws.cloudformation.model

import com.intellij.aws.cloudformation.IndentWriter

abstract class CfnNamedNode(val name: CfnNameNode) : CfnNode() {
  abstract override fun dump(writer: IndentWriter)
}