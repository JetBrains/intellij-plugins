package com.intellij.aws.cloudformation.model

class CfnResourcesNode(name: CfnStringValueNode?, val resources: List<CfnResourceNode>) : CfnNamedNode(name)
class CfnOutputsNode(name: CfnStringValueNode?, val properties: List<Pair<CfnStringValueNode, CfnExpressionNode>>) : CfnNamedNode(name)

class CfnParameterNode(name: CfnStringValueNode?, val properties: List<CfnNameValueNode>) : CfnNamedNode(name)
class CfnParametersNode(name: CfnStringValueNode?, val parameters: List<CfnParameterNode>) : CfnNamedNode(name)
