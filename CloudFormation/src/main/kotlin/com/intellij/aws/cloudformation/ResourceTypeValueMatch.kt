package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.model.CfnNameNode
import com.intellij.aws.cloudformation.model.CfnResourceNode
import com.intellij.aws.cloudformation.model.CfnResourceTypeNode
import com.intellij.psi.PsiElement

class ResourceTypeValueMatch private constructor(
    val valueNode: CfnNameNode,
    val resource: CfnResourceNode) {
  companion object {
    fun match(position: PsiElement, parsed: CloudFormationParsedFile): ResourceTypeValueMatch? {
      val valueNode = parsed.getCfnNode(position) as? CfnNameNode ?: return null

      val resourceTypeNode = CloudFormationPsiUtils.getParent(valueNode, parsed)
      if (resourceTypeNode !is CfnResourceTypeNode || resourceTypeNode.value != valueNode) {
        return null
      }

      val resourceNode = CloudFormationPsiUtils.getParent(resourceTypeNode, parsed) as? CfnResourceNode ?: return null
      if (resourceNode.type != resourceTypeNode) {
        return null
      }

      return ResourceTypeValueMatch(valueNode, resourceNode)
    }
  }
}