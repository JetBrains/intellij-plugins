package com.intellij.aws.cloudformation.model

class CfnResourcePropertiesNode(name: CfnScalarValueNode?, val properties: List<CfnResourcePropertyNode>) : CfnNamedNode(name)
class CfnResourceDependsOnNode(name: CfnScalarValueNode?, val dependsOn: List<CfnScalarValueNode>) : CfnNamedNode(name)
class CfnResourceConditionNode(name: CfnScalarValueNode?, val condition: CfnScalarValueNode?) : CfnNamedNode(name)
