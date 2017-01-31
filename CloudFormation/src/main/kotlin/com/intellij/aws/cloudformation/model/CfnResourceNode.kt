package com.intellij.aws.cloudformation.model

class CfnResourceNode(name: CfnScalarValueNode?,
                      val type: CfnResourceTypeNode?,
                      val properties: CfnResourcePropertiesNode?,
                      val condition: CfnResourceConditionNode?,
                      val dependsOn: CfnResourceDependsOnNode?,
                      val allTopLevelProperties: Map<String, CfnNamedNode>) : CfnNamedNode(name) {
  val typeName: String?
    get() = (type?.value as? CfnScalarValueNode)?.value
}