package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.model.CfnResourceNode
import com.intellij.aws.cloudformation.model.CfnResourceTypeNode
import com.intellij.aws.cloudformation.model.CfnScalarValueNode
import com.intellij.psi.PsiElement

class ResourceTypeValueMatch private constructor(
    val valueNode: CfnScalarValueNode,
    val resource: CfnResourceNode) {
  companion object {
    fun match(position: PsiElement, parsed: CloudFormationParsedFile): ResourceTypeValueMatch? {
      val valueNode = parsed.getCfnNodes(position).ofType<CfnScalarValueNode>().singleOrNull() ?: return null

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