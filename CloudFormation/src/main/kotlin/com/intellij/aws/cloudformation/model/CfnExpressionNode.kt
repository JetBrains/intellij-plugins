package com.intellij.aws.cloudformation.model

import com.intellij.aws.cloudformation.CloudFormationIntrinsicFunctions

abstract class CfnExpressionNode : CfnNode()
abstract class CfnScalarValueNode : CfnExpressionNode()

class CfnNumberValueNode(val value: String) : CfnScalarValueNode()
class CfnBooleanValueNode(val value: Boolean) : CfnScalarValueNode()
class CfnStringValueNode(val value: String) : CfnScalarValueNode()
class CfnArrayValueNode(val items: List<CfnExpressionNode>) : CfnExpressionNode()

open class CfnObjectValueNode(val properties: List<Pair<CfnStringValueNode?, CfnExpressionNode?>>) : CfnExpressionNode()
class CfnFunctionNode(
    val name: CfnStringValueNode,
    val functionId: CloudFormationIntrinsicFunctions,
    val args: List<CfnExpressionNode?>) : CfnExpressionNode()
