package com.intellij.aws.cloudformation.model

class CfnResourceNode(name: CfnScalarValueNode?,
                      val type: CfnResourceTypeNode?,
                      val allTopLevelProperties: Map<String, CfnNamedNode>,
                      val properties: CfnResourcePropertiesNode?) : CfnNamedNode(name) {
  val typeName: String?
    get() = (type?.value as? CfnScalarValueNode)?.value
}