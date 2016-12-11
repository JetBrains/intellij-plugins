package com.intellij.aws.cloudformation.model

import com.intellij.aws.cloudformation.IndentWriter

abstract class CfnNode() {
  abstract fun dump(writer: IndentWriter)
}