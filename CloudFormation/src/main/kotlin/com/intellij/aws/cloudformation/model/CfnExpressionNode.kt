package com.intellij.aws.cloudformation.model

import com.intellij.aws.cloudformation.CloudFormationIntrinsicFunction

abstract class CfnExpressionNode : CfnNode()

class CfnScalarValueNode(val value: String) : CfnExpressionNode()
class CfnArrayValueNode(val items: List<CfnExpressionNode>) : CfnExpressionNode()

open class CfnObjectValueNode(val properties: List<CfnNameValueNode>) : CfnExpressionNode()
class CfnFunctionNode(
    val name: CfnScalarValueNode,
    val functionId: CloudFormationIntrinsicFunction,
    val args: List<CfnExpressionNode?>) : CfnExpressionNode()
