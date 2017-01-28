package com.intellij.aws.cloudformation.model

class CfnResourcesNode(name: CfnScalarValueNode?, val resources: List<CfnResourceNode>) : CfnNamedNode(name)
class CfnOutputsNode(name: CfnScalarValueNode?, val properties: List<Pair<CfnScalarValueNode?, CfnExpressionNode?>>) : CfnNamedNode(name)

class CfnParameterNode(name: CfnScalarValueNode?, val properties: List<CfnNameValueNode>) : CfnNamedNode(name)
class CfnParametersNode(name: CfnScalarValueNode?, val parameters: List<CfnParameterNode>) : CfnNamedNode(name)
