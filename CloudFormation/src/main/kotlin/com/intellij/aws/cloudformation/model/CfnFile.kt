package com.intellij.aws.cloudformation.model

open class CfnNode() {
  abstract fun dump(writer: Indent)
}

class CfnNameNode(val id: String) : CfnNode() {

}

class CfnRootNode(
    val resourcesNode: CfnResourcesNode?
) : CfnNode()

class CfnResourcesNode(val resources: List<CfnResourceNode>) : CfnNode()

class CfnResourceNode(val name: CfnNameNode, val type: CfnNameNode?, val properties: CfnPropertiesNode?) : CfnNode()

class CfnPropertiesNode(val properties: List<CfnProperty>) : CfnNode()

class CfnProperty(val name: CfnNameNode) : CfnNode()
