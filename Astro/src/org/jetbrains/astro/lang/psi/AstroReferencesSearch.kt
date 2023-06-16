// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.psi

import com.intellij.lang.ecmascript6.psi.ES6ExportSpecifierAlias
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.RequestResultProcessor
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.search.searches.ReferencesSearch.SearchParameters
import com.intellij.util.Processor

class AstroReferencesSearch : QueryExecutorBase<PsiReference, SearchParameters>(true) {
  override fun processQuery(queryParameters: SearchParameters, consumer: Processor<in PsiReference>) {
    val element = queryParameters.elementToSearch
    val identifier = (element as? JSElement)?.name ?: return
    val effectiveSearchScope = queryParameters.effectiveSearchScope

    if (effectiveSearchScope !is LocalSearchScope) return
    effectiveSearchScope.scope.forEach {
      queryParameters.optimizer.searchWord(identifier, LocalSearchScope(it.containingFile), UsageSearchContext.IN_CODE, true, element,
                                           AstroSearchProcessor(queryParameters))
    }
  }
}

private class AstroSearchProcessor(val queryParameters: SearchParameters) : RequestResultProcessor() {
  override fun processTextOccurrence(element: PsiElement, offsetInElement: Int, consumer: Processor<in PsiReference>): Boolean {
    val needle = queryParameters.elementToSearch
    return !(needle is ES6ExportSpecifierAlias && element is AstroHtmlTag && needle.name == element.descriptor?.qualifiedName)
  }
}