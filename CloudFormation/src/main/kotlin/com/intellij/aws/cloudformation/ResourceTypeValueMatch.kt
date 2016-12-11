package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.model.CfnNameNode
import com.intellij.aws.cloudformation.model.CfnResourceNode
import com.intellij.psi.PsiElement

class ResourceTypeValueMatch private constructor(
    val name: CfnNameNode,
    val resource: CfnResourceNode) {
  companion object {
    fun match(position: PsiElement, parsed: CloudFormationParsedFile): ResourceTypeValueMatch? {
      val nameNode = parsed.getCfnNode(position) as? CfnNameNode ?: return null

      val parent = CloudFormationPsiUtils.getParent(nameNode, parsed)
      if (parent is CfnResourceNode && parent.type == nameNode) {
        return ResourceTypeValueMatch(nameNode, parent)
      }

      return null
    }
  }
}