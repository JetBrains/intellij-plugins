package com.intellij.aws.cloudformation.references

import com.intellij.aws.cloudformation.CloudFormationParser
import com.intellij.aws.cloudformation.model.CfnScalarValueNode
import com.intellij.aws.cloudformation.ofType
import com.intellij.json.psi.JsonElementGenerator
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

abstract class CloudFormationReferenceBase(element: PsiElement) : PsiReferenceBase<PsiElement>(element) {
  val scalarNode: CfnScalarValueNode

  init {
    val parsed = CloudFormationParser.parse(element.containingFile)
    scalarNode = parsed.getCfnNodes(element).ofType<CfnScalarValueNode>().single()
  }

  override fun getVariants(): Array<Any> {
    return getCompletionVariants().toTypedArray()
  }

  override fun handleElementRename(newElementName: String): PsiElement {
    val newElement = JsonElementGenerator(myElement.project).createStringLiteral(newElementName)
    return myElement.replace(newElement)
  }

  abstract fun getCompletionVariants(): List<String>
}
