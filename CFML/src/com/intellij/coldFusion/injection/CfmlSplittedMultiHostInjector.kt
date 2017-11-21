package com.intellij.coldFusion.injection

import com.intellij.coldFusion.model.psi.CfmlLeafPsiElement
import com.intellij.coldFusion.model.psi.impl.CfmlTagImpl
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import java.util.*

/**
 * @author Sergey Karashevich
 */
class CfmlSplittedMultiHostInjector(project: Project) : MultiHostInjector {

  val splittedManager: CfmlSplittedInjectionManager = CfmlSplittedInjectionManager.getInstance(project)

  val NO_SPLITTED_INJECTION_TIMESTAMP: Key<Int> = Key.create("NO_SPLITTED_INJECTION_TIMESTAMP")

  override fun getLanguagesToInject(registrar: MultiHostRegistrar, psiElement: PsiElement) {
    if (splittedManager.mySplittedInjectors.isEmpty()) return

    val containingFile = psiElement.containingFile
    val project = containingFile.project
    val pair = getHeadAndSplittedElements(psiElement)
    val splittedElements: Array<PsiElement> = pair.second
    if (splittedElements.any { com.intellij.psi.util.PsiTreeUtil.isAncestor(psiElement, it, false) }) {
      getRegistrarWithLinkedSplittedElements(project, splittedElements, registrar)
    }
  }

  /**
   * Iterates all splittedInjectors from CfmlSplittedInjectionManager and updates registrar <Place, PsiFile> result
   */
  private fun getRegistrarWithLinkedSplittedElements(project: Project,
                                                     splittedElements: Array<PsiElement>,
                                                     registrar: MultiHostRegistrar): Boolean {

    val splittedManager = CfmlSplittedInjectionManager.getInstance(project)
    var injected = false
    for (splittedInjector in splittedManager.mySplittedInjectors) {
      injected = splittedInjector.getLanguagesToInject(registrar, *splittedElements)
      if (injected) break
    }

    return injected
  }

  fun getHeadAndSplittedElements(context: PsiElement): Pair<PsiElement, Array<PsiElement>> {
    val head: PsiElement? = findHead(context) { it is CfmlTagImpl && it.name?.toLowerCase() == "cfquery" }
    val filteredChildren: Array<PsiElement> =
      if (head != null)
        filterChildren(head) { it is CfmlLeafPsiElement }
      else
        emptyArray()
    return Pair<PsiElement, Array<PsiElement>>(head ?: context, filteredChildren)
  }

  //if there is no head checked by checker than return null
  private fun findHead(start: PsiElement, checker: (PsiElement) -> Boolean): PsiElement? {
    var psiElement = start
    while (psiElement.parent != null && !checker.invoke(psiElement)) psiElement = psiElement.parent
    if (checker.invoke(psiElement)) return psiElement
    else return null
  }

  private fun filterChildren(root: PsiElement, filter: (PsiElement) -> Boolean): Array<PsiElement> {
    val filteredList = ArrayList<PsiElement>()
    root.firstChild.traverseAndConsume { if (filter.invoke(it)) filteredList.add(it) }
    return filteredList.toTypedArray()
  }

  private fun PsiElement.traverseAndConsume(consumer: (PsiElement) -> Unit) {
    var psiElement = this
    while (psiElement.nextSibling != null) {
      consumer.invoke(psiElement)
      psiElement = psiElement.nextSibling
    }
  }

  override fun elementsToInjectIn(): List<Class<out PsiElement>> = listOf(CfmlTagImpl::class.java)

}