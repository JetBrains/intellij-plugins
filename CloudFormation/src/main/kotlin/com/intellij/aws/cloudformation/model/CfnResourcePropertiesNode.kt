package com.intellij.aws.cloudformation.model

class CfnResourcePropertiesNode(name: CfnScalarValueNode?, val properties: List<CfnResourcePropertyNode>) : CfnNamedNode(name)