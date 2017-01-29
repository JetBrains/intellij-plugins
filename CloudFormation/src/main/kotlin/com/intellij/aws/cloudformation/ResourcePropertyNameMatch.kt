package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.model.CfnResourceNode
import com.intellij.aws.cloudformation.model.CfnResourcePropertiesNode
import com.intellij.aws.cloudformation.model.CfnResourcePropertyNode
import com.intellij.aws.cloudformation.model.CfnScalarValueNode
import com.intellij.psi.PsiElement

class ResourcePropertyNameMatch private constructor(
    val name: CfnScalarValueNode,
    val property: CfnResourcePropertyNode,
    val properties: CfnResourcePropertiesNode,
    val resource: CfnResourceNode) {
  companion object {
    fun match(position: PsiElement, parsed: CloudFormationParsedFile): ResourcePropertyNameMatch? {
      val nameNode = parsed.getCfnNodes(position).ofType<CfnScalarValueNode>().singleOrNull() ?: return null

      val propertyNode = CloudFormationPsiUtils.getParent(nameNode, parsed) as? CfnResourcePropertyNode ?: return null
      val propertiesNode = CloudFormationPsiUtils.getParent(propertyNode, parsed) as? CfnResourcePropertiesNode ?: return null
      val resourceNode = CloudFormationPsiUtils.getParent(propertiesNode, parsed) as? CfnResourceNode ?: return null

      if (propertyNode.name == nameNode) {
        return ResourcePropertyNameMatch(nameNode, propertyNode, propertiesNode, resourceNode)
      }

      return null
    }
  }
}