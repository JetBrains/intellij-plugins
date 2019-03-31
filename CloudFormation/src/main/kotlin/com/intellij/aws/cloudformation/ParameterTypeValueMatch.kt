package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.model.CfnNameValueNode
import com.intellij.aws.cloudformation.model.CfnParameterNode
import com.intellij.aws.cloudformation.model.CfnScalarValueNode
import com.intellij.psi.PsiElement

class ParameterTypeValueMatch private constructor(
    val valueNode: CfnScalarValueNode,
    val parameter: CfnParameterNode) {
  companion object {
    fun match(position: PsiElement, parsed: CloudFormationParsedFile): ParameterTypeValueMatch? {
      val valueNode = parsed.getCfnNodes(position).ofType<CfnScalarValueNode>().singleOrNull() ?: return null

      val parameterTypeNode = valueNode.parent(parsed)
      if (parameterTypeNode !is CfnNameValueNode ||
          parameterTypeNode.value != valueNode ||
          parameterTypeNode.name?.value != CloudFormationConstants.ParameterTypePropertyName) {
        return null
      }

      val parameterNode = parameterTypeNode.parent(parsed) as? CfnParameterNode ?: return null

      return ParameterTypeValueMatch(valueNode, parameterNode)
    }
  }
}