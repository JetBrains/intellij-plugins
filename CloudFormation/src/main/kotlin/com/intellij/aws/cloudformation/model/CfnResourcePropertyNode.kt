package com.intellij.aws.cloudformation.model

class CfnResourcePropertyNode(name: CfnScalarValueNode?, val value: CfnExpressionNode?) : CfnNamedNode(name)