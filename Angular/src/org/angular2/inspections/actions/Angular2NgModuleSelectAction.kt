// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.actions

import com.intellij.lang.javascript.modules.imports.ES6ImportExecutorFactory.FACTORY
import com.intellij.lang.javascript.modules.imports.JSImportAction
import com.intellij.lang.javascript.modules.imports.JSImportCandidate
import com.intellij.lang.javascript.modules.imports.JSImportCandidateWithExecutor
import com.intellij.lang.javascript.modules.imports.filter.JSImportCandidatesFilter
import com.intellij.lang.javascript.modules.imports.filter.NestedLibraryModulesFilter
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.NlsContexts
import com.intellij.psi.PsiElement
import org.angular2.entities.Angular2Declaration
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.entities.Angular2Module

open class Angular2NgModuleSelectAction(
  editor: Editor?,
  context: PsiElement,
  name: String,
  private val myActionName: @NlsContexts.Command String,
  protected val myCodeCompletion: Boolean,
) : JSImportAction(editor, context, name) {

  override fun filterAndSort(
    candidates: List<JSImportCandidate>,
    place: PsiElement,
  ): List<JSImportCandidateWithExecutor> {
    val isStandalone = Angular2EntitiesProvider.findTemplateComponent(context)?.isStandalone == true
    val result = filter(candidates).map { JSImportCandidateWithExecutor(it, FACTORY.createExecutor(place)) }
    if (result.size <= 1) return result
    return result
      .asSequence()
      .map { Pair(it, isStandalone(it)) }
      .sortedBy { it.second != isStandalone }
      .map { it.first }
      .toList()
  }

  override fun getImportCandidatesFilterFactories(): List<JSImportCandidatesFilter.FilterFactory> =
    super.getImportCandidatesFilterFactories().filter { it !is NestedLibraryModulesFilter.Factory }

  override fun getName(): String {
    return myActionName
  }

  override fun getDebugNameForElement(element: JSImportCandidateWithExecutor): String {
    val psiElement = element.element
    if (psiElement !is JSElement) return super.getDebugNameForElement(element)
    val candidate = element.candidate
    val text = candidate.containerText

    return psiElement.name + " - " + text
  }

  override fun shouldShowPopup(candidates: List<JSImportCandidateWithExecutor>): Boolean {
    return myCodeCompletion || super.shouldShowPopup(candidates)
  }

  private fun isStandalone(candidate: JSImportCandidateWithExecutor): Boolean =
    candidate.element
      ?.let { Angular2EntitiesProvider.getEntity(it) }
      ?.let {
        when (it) {
          is Angular2Module -> it.isStandalonePseudoModule
          is Angular2Declaration -> it.isStandalone
          else -> false
        }
      } == true
}
