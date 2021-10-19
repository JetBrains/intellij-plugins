// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.findUsages

import com.intellij.find.usages.api.PsiUsage
import com.intellij.find.usages.api.Usage
import com.intellij.find.usages.api.UsageSearchParameters
import com.intellij.find.usages.api.UsageSearcher
import com.intellij.javascript.web.codeInsight.WebSymbolReference
import com.intellij.javascript.web.symbols.WebSymbolDelegate.Companion.unwrapAllDelegates
import com.intellij.lang.Language
import com.intellij.lang.html.HtmlCompatibleMetaLanguage
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.model.Pointer
import com.intellij.model.psi.PsiExternalReferenceHost
import com.intellij.model.psi.PsiSymbolReferenceHints
import com.intellij.model.psi.PsiSymbolReferenceService
import com.intellij.model.search.LeafOccurrence
import com.intellij.model.search.LeafOccurrenceMapper
import com.intellij.model.search.SearchContext
import com.intellij.model.search.SearchService
import com.intellij.openapi.components.service
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.walkUp
import com.intellij.refactoring.suggested.startOffset
import com.intellij.util.Query
import org.angular2.entities.Angular2DirectiveSelectorSymbol
import org.angular2.web.Angular2SymbolDelegate

class Angular2SelectorUsageSearcher : UsageSearcher {
  override fun collectSearchRequest(parameters: UsageSearchParameters): Query<out Usage>? =
    (parameters.target as? Angular2DirectiveSelectorSymbol)
      ?.let {
        buildSelectorUsagesQuery(it, parameters.searchScope)
      }

  companion object {
    fun buildSelectorUsagesQuery(symbol: Angular2DirectiveSelectorSymbol, searchScope: SearchScope) =
      SearchService.getInstance()
        .searchWord(symbol.project, symbol.name)
        .caseSensitive(true)
        //.inFilesWithLanguageOfKind(Language.findInstance(HtmlCompatibleMetaLanguage::class.java))
        //.inFilesWithLanguageOfKind(JavascriptLanguage.INSTANCE)
        .inContexts(SearchContext.IN_CODE_HOSTS, SearchContext.IN_CODE, SearchContext.IN_PLAIN_TEXT, SearchContext.IN_STRINGS)
        .inScope(searchScope)
        .buildQuery(LeafOccurrenceMapper.withPointer(symbol.createPointer(), ::findReferencesToSymbol))

    private fun findReferencesToSymbol(symbol: Angular2DirectiveSelectorSymbol, leafOccurrence: LeafOccurrence): Collection<PsiUsage> =
      service<PsiSymbolReferenceService>().run {

        for ((element, offsetInElement) in walkUp(leafOccurrence.start, leafOccurrence.offsetInStart, leafOccurrence.scope)) {
          if (element !is PsiExternalReferenceHost)
            continue

          if (symbol.source == element && symbol.textRangeInSource.contains(offsetInElement)) {
            return listOf(Angular2SelectorPsiUsage(
              symbol.source.containingFile, symbol.textRangeInSource.shiftRight(element.startOffset), true))
          }

          val foundReferences = getReferences(element, PsiSymbolReferenceHints.offsetHint(offsetInElement))
            .asSequence()
            .filterIsInstance<WebSymbolReference>()
            .filter { it.rangeInElement.containsOffset(offsetInElement) }
            .filter { ref ->
              ref.resolveReference()
                .asSequence()
                .map { it.unwrapAllDelegates() }
                .filterIsInstance<Angular2DirectiveSelectorSymbol>()
                .any { it == symbol }
            }
            .map {
              Angular2SelectorPsiUsage(it.element.containingFile, it.absoluteRange, false)
            }
            .toList()

          return foundReferences
        }

        emptyList()
      }

    private class Angular2SelectorPsiUsage(override val file: PsiFile,
                                           override val range: TextRange,
                                           override val declaration: Boolean) : PsiUsage {

      override fun createPointer(): Pointer<out PsiUsage> {
        val pointer = SmartPointerManager.getInstance(file.project).createSmartPsiFileRangePointer(file, range)
        return Pointer {
          val file: PsiFile = pointer.element ?: return@Pointer null
          val range: TextRange = pointer.range?.let(TextRange::create) ?: return@Pointer null
          Angular2SelectorPsiUsage(file, range, declaration)
        }
      }
    }
  }
}