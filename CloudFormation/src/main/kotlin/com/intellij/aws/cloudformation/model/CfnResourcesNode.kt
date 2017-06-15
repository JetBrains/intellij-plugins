package com.intellij.aws.cloudformation.model

class CfnMetadataNode(name: CfnScalarValueNode?, val value: CfnObjectValueNode?): CfnNamedNode(name)

class CfnResourcesNode(name: CfnScalarValueNode?, val resources: List<CfnResourceNode>) : CfnNamedNode(name)

class CfnOutputNode(name: CfnScalarValueNode?, value: CfnExpressionNode?) : CfnNameValueNode(name, value)
class CfnOutputsNode(name: CfnScalarValueNode?, val properties: List<CfnOutputNode>) : CfnNamedNode(name)

class CfnConditionNode(name: CfnScalarValueNode?, value: CfnExpressionNode?) : CfnNameValueNode(name, value)
class CfnConditionsNode(name: CfnScalarValueNode?, val conditions: List<CfnConditionNode>) : CfnNamedNode(name)

class CfnParameterNode(name: CfnScalarValueNode?, val properties: List<CfnNameValueNode>) : CfnNamedNode(name)
class CfnParametersNode(name: CfnScalarValueNode?, val parameters: List<CfnParameterNode>) : CfnNamedNode(name)

class CfnMappingValue(name: CfnScalarValueNode?, value: CfnExpressionNode?) : CfnNameValueNode(name, value)
class CfnSecondLevelMappingNode(name: CfnScalarValueNode?, val secondLevelMapping: List<CfnMappingValue>) : CfnNamedNode(name)
class CfnFirstLevelMappingNode(name: CfnScalarValueNode?, val firstLevelMapping: List<CfnSecondLevelMappingNode>) : CfnNamedNode(name)
class CfnMappingsNode(name: CfnScalarValueNode?, val mappings: List<CfnFirstLevelMappingNode>) : CfnNamedNode(name)
