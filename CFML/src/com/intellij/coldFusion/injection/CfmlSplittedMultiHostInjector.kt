package com.intellij.coldFusion.injection

import com.intellij.coldFusion.model.psi.CfmlLeafPsiElement
import com.intellij.coldFusion.model.psi.impl.CfmlTagImpl
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

class CfmlSplittedMultiHostInjector(project: Project) : MultiHostInjector {

  private val splittedManager: CfmlSplittedInjectionManager = CfmlSplittedInjectionManager.getInstance(project)

  override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
    if (splittedManager.mySplittedInjectors.isEmpty()) return

    val containingFile = context.containingFile
    val project = containingFile.project
    val (_, splittedElements) = getHeadAndSplittedElements(context)
//    if (splittedElements.any { PsiTreeUtil.isAncestor(context, it, false) })
      processInjections(project, splittedElements, registrar)
  }

  override fun elementsToInjectIn(): List<Class<out PsiElement>> = listOf(CfmlTagImpl::class.java)

  /**
   * Iterates all splittedInjectors from CfmlSplittedInjectionManager and updates registrar <Place, PsiFile> result
   */
  private fun processInjections(project: Project,
                                splittedElements: List<PsiElement>,
                                registrar: MultiHostRegistrar): Boolean {
    val splittedManager = CfmlSplittedInjectionManager.getInstance(project)
    return splittedManager.mySplittedInjectors.any { it.processInjection(registrar, splittedElements) }
  }




  private fun getHeadAndSplittedElements(context: PsiElement): Pair<PsiElement, List<PsiElement>> {
    if (context.isCfQueryTag()) return getHeadAndSplittedElementsForCfQuery(context)
    if (context.isCfElseTagInsideCfQuery() || context.isCfIfElseTagInsideCfQuery()) return getHeadAndSplittedElementsForCfElseOrCfIfElse(context)
    return Pair(context, emptyList())
  }

  private fun getHeadAndSplittedElementsForCfElseOrCfIfElse(context: PsiElement): Pair<PsiElement, List<PsiElement>> {
    if (context.nextSibling is CfmlLeafPsiElement) return Pair(context, listOf(context.nextSibling))
    return Pair(context, emptyList())
  }

  private fun getHeadAndSplittedElementsForCfQuery(context: PsiElement): Pair<PsiElement, List<PsiElement>> {
    val filteredChildren = mutableListOf<PsiElement>()
    context.firstChild.traverse().forEach {
      if (it is CfmlLeafPsiElement) filteredChildren.add(it)
      if (it.isCfifTag()) filteredChildren.addNotNull(getFirstCfifValue(it as CfmlTagImpl))
    }
    return Pair(context, filteredChildren)
  }

}