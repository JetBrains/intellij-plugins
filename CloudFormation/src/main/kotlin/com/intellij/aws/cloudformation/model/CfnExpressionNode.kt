package com.intellij.aws.cloudformation.model

import com.intellij.aws.cloudformation.CloudFormationIntrinsicFunctions

abstract class CfnExpressionNode : CfnNode()

class CfnScalarValueNode(val value: String) : CfnExpressionNode()
class CfnArrayValueNode(val items: List<CfnExpressionNode>) : CfnExpressionNode()

open class CfnObjectValueNode(val properties: List<Pair<CfnScalarValueNode?, CfnExpressionNode?>>) : CfnExpressionNode()
class CfnFunctionNode(
    val name: CfnScalarValueNode,
    val functionId: CloudFormationIntrinsicFunctions,
    val args: List<CfnExpressionNode?>) : CfnExpressionNode()
