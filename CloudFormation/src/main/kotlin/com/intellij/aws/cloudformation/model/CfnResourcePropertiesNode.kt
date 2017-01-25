package com.intellij.aws.cloudformation.model

class CfnResourcePropertiesNode(name: CfnStringValueNode?, val properties: List<CfnResourcePropertyNode>) : CfnNamedNode(name)