package com.intellij.aws.cloudformation.model

class CfnResourceNode(name: CfnStringValueNode,
                      val type: CfnResourceTypeNode?,
                      val allTopLevelProperties: Map<String, CfnNamedNode>,
                      val properties: CfnResourcePropertiesNode?) : CfnNamedNode(name) {
  val typeName: String?
    get() = (type?.value as? CfnStringValueNode)?.value
}