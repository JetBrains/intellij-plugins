package com.intellij.aws.cloudformation.model

abstract class CfnExpressionNode() : CfnNode()

class CfnMissingOrInvalidValueNode() : CfnExpressionNode()
class CfnNumberValueNode(val value: String) : CfnExpressionNode()
class CfnBooleanValueNode(val value: Boolean) : CfnExpressionNode()
class CfnStringValueNode(val value: String) : CfnExpressionNode()
class CfnArrayValueNode(val items: List<CfnExpressionNode>) : CfnExpressionNode()

open class CfnObjectValueNode(val properties: List<Pair<CfnStringValueNode, CfnExpressionNode>>) : CfnExpressionNode()
class CfnFunctionNode(val name: CfnStringValueNode, val args: List<CfnExpressionNode>) : CfnExpressionNode()
