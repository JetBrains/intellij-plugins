package com.intellij.aws.cloudformation.model

class CfnResourcePropertyNode(name: CfnStringValueNode, val value: CfnExpressionNode) : CfnNamedNode(name)