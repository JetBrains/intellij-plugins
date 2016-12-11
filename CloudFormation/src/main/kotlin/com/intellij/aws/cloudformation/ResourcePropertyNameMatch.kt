package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.model.CfnNameNode
import com.intellij.aws.cloudformation.model.CfnPropertiesNode
import com.intellij.aws.cloudformation.model.CfnPropertyNode
import com.intellij.aws.cloudformation.model.CfnResourceNode
import com.intellij.psi.PsiElement

class ResourcePropertyNameMatch private constructor(
    val name: CfnNameNode,
    val property: CfnPropertyNode,
    val properties: CfnPropertiesNode,
    val resource: CfnResourceNode) {
  companion object {
    fun match(position: PsiElement, parsed: CloudFormationParsedFile): ResourcePropertyNameMatch? {
      val nameNode = parsed.getCfnNode(position) as? CfnNameNode ?: return null

      val propertyNode = CloudFormationPsiUtils.getParent(nameNode, parsed) as? CfnPropertyNode ?: return null
      val propertiesNode = CloudFormationPsiUtils.getParent(propertyNode, parsed) as? CfnPropertiesNode ?: return null
      val resourceNode = CloudFormationPsiUtils.getParent(propertiesNode, parsed) as? CfnResourceNode ?: return null

      if (propertyNode.name == nameNode) {
        return ResourcePropertyNameMatch(nameNode, propertyNode, propertiesNode, resourceNode)
      }

      return null
    }
  }
}