package com.intellij.aws.cloudformation.model

open class CfnNameValueNode(name: CfnScalarValueNode?, val value: CfnExpressionNode?) : CfnNamedNode(name)