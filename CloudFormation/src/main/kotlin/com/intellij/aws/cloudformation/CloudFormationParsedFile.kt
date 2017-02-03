package com.intellij.aws.cloudformation

import com.google.common.collect.Multimap
import com.intellij.aws.cloudformation.model.CfnNode
import com.intellij.aws.cloudformation.model.CfnRootNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

class CloudFormationParsedFile(val problems: List<CloudFormationProblem>,
                               private val node2psi: Map<CfnNode, PsiElement>,
                               private val psi2node: Multimap<PsiElement, CfnNode>,
                               val root: CfnRootNode,
                               val psiFile: PsiFile,
                               val fileModificationStamp: Long) {

  fun getCfnNodes(psiElement: PsiElement): Collection<CfnNode> = psi2node.get(psiElement)
  fun getPsiElement(node: CfnNode): PsiElement = node2psi[node]!!
}
