package com.intellij.coldFusion.injection

import com.intellij.coldFusion.model.psi.CfmlLeafPsiElement
import com.intellij.coldFusion.model.psi.impl.CfmlTagImpl
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.PsiParameterizedCachedValue
import com.intellij.psi.impl.source.tree.injected.MultiHostRegistrarImpl
import com.intellij.psi.util.*

/**
 * @author Sergey Karashevich
 */
class CfmlSplittedMultiHostInjectorAdapter(project: Project) : MultiHostInjector {

  val splittedManager: CfmlSplittedInjectionManager = CfmlSplittedInjectionManager.getInstance(project)

  val INJECTED_PSI_IN_SPLITTED: Key<ParameterizedCachedValue<MultiHostRegistrarImpl, PsiElement>> = Key.create("INJECTED_PSI_IN_SPLITTED")
  val NO_SPLITTED_INJECTION_TIMESTAMP: Key<Int> = Key.create("NO_SPLITTED_INJECTION_TIMESTAMP")

  override fun getLanguagesToInject(registrar: MultiHostRegistrar, psiElement: PsiElement) {
    if (splittedManager.mySplittedInjectors.isEmpty()) return
    if (registrar !is MultiHostRegistrarImpl) throw Exception("Unable to cast MultiHostRegistrar: $registrar to MultiHostRegistrarImpl")

    val containingFile = registrar.hostPsiFile
    val project = containingFile.project
    val modificationCount = PsiManager.getInstance(project).modificationTracker.modificationCount
    val pair = getHeadAndSplittedElements(psiElement)
    val head = pair.first
    val splittedElements = pair.second
    val noInjectionTimestamp = head.getUserData(NO_SPLITTED_INJECTION_TIMESTAMP)

    val myMultiHostRegistrar: MultiHostRegistrarImpl?
    var cachedPsiElement2MultiHostRegistrar: ParameterizedCachedValue<MultiHostRegistrarImpl, PsiElement>? = null

    if (splittedElements.isEmpty() || noInjectionTimestamp != null && modificationCount.compareTo(noInjectionTimestamp) == 0) {
      myMultiHostRegistrar = null
    }
    else {
      cachedPsiElement2MultiHostRegistrar = head.getUserData(INJECTED_PSI_IN_SPLITTED)
      myMultiHostRegistrar = cachedPsiElement2MultiHostRegistrar?.getValue(psiElement)
                             ?: getRegistrarWithLinkedSplittedElements(containingFile, project, head, splittedElements)
    }

    if (myMultiHostRegistrar != null && myMultiHostRegistrar.result != null) {
      registrar.copyResult(myMultiHostRegistrar)

      if (cachedPsiElement2MultiHostRegistrar == null) {
        val cachedResult = CachedValueProvider.Result.create(myMultiHostRegistrar, PsiModificationTracker.MODIFICATION_COUNT,
                                                             CfmlSplittedInjectionManager.getInstance(project))
        cachedPsiElement2MultiHostRegistrar = createCachedPsiElement2MultiHostRegistrar(project)
        cachedPsiElement2MultiHostRegistrar.setValue(cachedResult)

        head.putUserData(INJECTED_PSI_IN_SPLITTED, cachedPsiElement2MultiHostRegistrar)
        if (head.getUserData(NO_SPLITTED_INJECTION_TIMESTAMP) != null) head.putUserData(NO_SPLITTED_INJECTION_TIMESTAMP, null)
      }
    }
    else {
      // cache no-injection flag
      if (head.getUserData(INJECTED_PSI_IN_SPLITTED) != null) head.putUserData(INJECTED_PSI_IN_SPLITTED, null)
      head.putUserData(NO_SPLITTED_INJECTION_TIMESTAMP, modificationCount.toInt())
    }
  }

  private fun MultiHostRegistrarImpl.copyResult(multiHostRegistrarImpl: MultiHostRegistrarImpl) {
    multiHostRegistrarImpl.result.forEach { place2psiFile ->  this.addToResults( place2psiFile.first, place2psiFile.second, multiHostRegistrarImpl) }
  }

  private fun createCachedPsiElement2MultiHostRegistrar(project: Project): PsiParameterizedCachedValue<MultiHostRegistrarImpl, PsiElement> {
    val result: ParameterizedCachedValue<MultiHostRegistrarImpl, PsiElement>? = CachedValuesManager.getManager(project).createParameterizedCachedValue(
      { context ->
        val containingFile = context.containingFile
        val pair = getHeadAndSplittedElements(context)
        val registrar = if (pair.second.isEmpty()) null else getRegistrarWithLinkedSplittedElements(containingFile, containingFile.project, pair.first, pair.second)
        if (registrar != null)
          CachedValueProvider.Result.create<MultiHostRegistrarImpl>(registrar, PsiModificationTracker.MODIFICATION_COUNT,
                                                                    CfmlSplittedInjectionManager.getInstance(containingFile.project))
        else null
      }, false)
    return result as PsiParameterizedCachedValue<MultiHostRegistrarImpl, PsiElement>
  }

  /**
   * Iterates all splittedInjectors from CfmlSplittedInjectionManager and updates registrar <Place, PsiFile> result
   */
  private fun getRegistrarWithLinkedSplittedElements(containingFile: PsiFile,
                                                     project: Project,
                                                     head: PsiElement,
                                                     splittedElements: Array<PsiElement>): MultiHostRegistrarImpl? {

    var registrar: MultiHostRegistrarImpl? = MultiHostRegistrarImpl(project, containingFile, head)
    val splittedManager = CfmlSplittedInjectionManager.getInstance(project)
    for (splittedInjector in splittedManager.mySplittedInjectors) {
      splittedInjector.getLanguagesToInject(registrar!!, *splittedElements)
      if (registrar.result != null) break
    }

    if (registrar!!.result == null) registrar = null
    return registrar
  }

  fun getHeadAndSplittedElements(context: PsiElement): Pair<PsiElement, Array<PsiElement>> {
    val filteredElements: List<PsiElement>
    if (context is CfmlTagImpl && context.name == "cfquery") {
      filteredElements = PsiTreeUtil.collectElements(context, PsiElementFilter { element -> element is CfmlLeafPsiElement }).toList()
    } else {
      filteredElements = emptyList()
    }
    return Pair<PsiElement, Array<PsiElement>>(context, filteredElements.toTypedArray())

  }

  override fun elementsToInjectIn(): List<Class<out PsiElement>> = listOf(CfmlTagImpl::class.java)

}