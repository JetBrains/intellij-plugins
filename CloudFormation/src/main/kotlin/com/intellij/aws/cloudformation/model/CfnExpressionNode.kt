package com.intellij.aws.cloudformation.model

abstract class CfnExpressionNode : CfnNode()
abstract class CfnScalarValueNode : CfnExpressionNode()

class CfnMissingOrInvalidValueNode : CfnScalarValueNode()
class CfnNumberValueNode(val value: String) : CfnScalarValueNode()
class CfnBooleanValueNode(val value: Boolean) : CfnScalarValueNode()
class CfnStringValueNode(val value: String) : CfnScalarValueNode()
class CfnArrayValueNode(val items: List<CfnExpressionNode>) : CfnExpressionNode()

open class CfnObjectValueNode(val properties: List<Pair<CfnStringValueNode, CfnExpressionNode>>) : CfnExpressionNode()
class CfnFunctionNode(val name: CfnStringValueNode, val args: List<CfnExpressionNode>) : CfnExpressionNode()
