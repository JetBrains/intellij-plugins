// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.findUsages

import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.QuerySearchRequest
import com.intellij.psi.search.SearchRequestCollector
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.util.PairProcessor
import com.intellij.util.Processor
import org.jetbrains.vuejs.codeInsight.findModule
import org.jetbrains.vuejs.codeInsight.toAsset
import org.jetbrains.vuejs.refactoring.VueRefactoringUtils

/**
 * @author Artem.Gainanov
 */
class VueJSReferenceSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {

  override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
    val element = queryParameters.elementToSearch
    val component = VueRefactoringUtils.getComponent(element)

    if (component != null) {
      val content = findModule(element) ?: return
      val defaultExport = ES6PsiUtil.findDefaultExport(content) as PsiElement
      val collector = SearchRequestCollector(queryParameters.optimizer.searchSession)
      queryParameters.optimizer.searchQuery(
        QuerySearchRequest(ReferencesSearch.search(defaultExport, queryParameters.effectiveSearchScope), collector,
                           false, PairProcessor { reference, _ -> consumer.process(reference) }))
      //We are searching for <component-a> and <ComponentA> tags
      //Original component name can't be fromAsset (name: "component-a")
      val names = setOf(component.name, toAsset(component.name))
      for (name in names) {
        queryParameters.optimizer.searchWord(name, queryParameters.effectiveSearchScope, false,
                                             component)
      }
    }
  }

}