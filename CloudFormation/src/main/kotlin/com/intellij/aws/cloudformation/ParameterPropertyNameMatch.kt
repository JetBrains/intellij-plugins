package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.model.CfnNameValueNode
import com.intellij.aws.cloudformation.model.CfnParameterNode
import com.intellij.aws.cloudformation.model.CfnScalarValueNode
import com.intellij.psi.PsiElement

class ParameterPropertyNameMatch private constructor(
    val name: CfnScalarValueNode,
    val property: CfnNameValueNode,
    val parameter: CfnParameterNode) {
  companion object {
    fun match(position: PsiElement, parsed: CloudFormationParsedFile): ParameterPropertyNameMatch? {
      val nameNode = parsed.getCfnNodes(position).ofType<CfnScalarValueNode>().singleOrNull() ?: return null
      val propertyNode = nameNode.parent(parsed) as? CfnNameValueNode ?: return null
      val parameterNode = propertyNode.parent(parsed) as? CfnParameterNode ?: return null

      if (propertyNode.name == nameNode) {
        return ParameterPropertyNameMatch(nameNode, propertyNode, parameterNode)
      }

      return null
    }
  }
}