package com.intellij.aws.cloudformation

import com.google.common.collect.HashBiMap
import com.intellij.aws.cloudformation.model.CfnNode
import com.intellij.aws.cloudformation.model.CfnRootNode
import com.intellij.psi.PsiElement

class CloudFormationParsedFile(val problems: List<ParseProblem>,
                               val nodesMap: HashBiMap<PsiElement, CfnNode>,
                               val root: CfnRootNode) {
  class ParseProblem(val element: PsiElement, val description: String)

  fun getCfnNode(psiElement: PsiElement): CfnNode? = nodesMap[psiElement]
  fun getPsiElement(node: CfnNode): PsiElement = nodesMap.inverse()[node]!!
}

