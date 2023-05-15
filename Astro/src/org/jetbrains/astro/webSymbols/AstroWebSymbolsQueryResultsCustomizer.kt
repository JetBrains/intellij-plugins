// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.webSymbols

import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.asSafely
import com.intellij.webSymbols.PsiSourcedWebSymbol
import com.intellij.webSymbols.SymbolKind
import com.intellij.webSymbols.SymbolNamespace
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.context.WebSymbolsContext
import com.intellij.webSymbols.query.WebSymbolsQueryResultsCustomizer
import com.intellij.webSymbols.query.WebSymbolsQueryResultsCustomizerFactory
import org.jetbrains.astro.AstroFramework
import org.jetbrains.astro.codeInsight.completion.AstroImportInsertHandler
import org.jetbrains.astro.lang.AstroFileImpl
import org.jetbrains.astro.webSymbols.AstroQueryConfigurator.Companion.PROP_ASTRO_PROXIMITY
import org.jetbrains.astro.webSymbols.symbols.AstroComponent

class AstroWebSymbolsQueryResultsCustomizer(private val context: PsiElement) : WebSymbolsQueryResultsCustomizer {

  override fun apply(matches: List<WebSymbol>,
                     strict: Boolean,
                     namespace: SymbolNamespace?,
                     kind: SymbolKind,
                     name: String?): List<WebSymbol> {
    if (namespace != WebSymbol.NAMESPACE_HTML && kind != WebSymbol.KIND_HTML_ELEMENTS)
      return matches
    return matches.filter { symbol ->
      symbol.asSafely<AstroComponent>()?.source != context.containingFile.originalFile
      && (!strict || symbol.properties[PROP_ASTRO_PROXIMITY].let {
        it == null || it == AstroProximity.LOCAL
      })
    }
  }

  override fun apply(item: WebSymbolCodeCompletionItem, namespace: SymbolNamespace?, kind: SymbolKind): WebSymbolCodeCompletionItem? {
    if (namespace == WebSymbol.NAMESPACE_HTML || kind == WebSymbol.KIND_HTML_ELEMENTS) {
      val proximity = item.symbol?.properties?.get(PROP_ASTRO_PROXIMITY)
      val element = (item.symbol as? PsiSourcedWebSymbol)?.source
      if (proximity == AstroProximity.OUT_OF_SCOPE && element is AstroFileImpl) {
        return if (element != context.containingFile.originalFile)
          item.withInsertHandlerAdded(AstroImportInsertHandler, WebSymbol.Priority.LOWEST)
        else null
      }
    }
    return item
  }

  override fun createPointer(): Pointer<out WebSymbolsQueryResultsCustomizer> {
    val contextPtr = context.createSmartPointer()
    return Pointer {
      contextPtr.dereference()?.let { AstroWebSymbolsQueryResultsCustomizer(it) }
    }
  }

  override fun getModificationCount(): Long = 0

  override fun equals(other: Any?): Boolean =
    other is AstroWebSymbolsQueryResultsCustomizer
    && other.context == context

  override fun hashCode(): Int =
    context.hashCode()


  class Provider : WebSymbolsQueryResultsCustomizerFactory {
    override fun create(location: PsiElement, context: WebSymbolsContext): WebSymbolsQueryResultsCustomizer? =
      if (context.framework == AstroFramework.ID)
        AstroWebSymbolsQueryResultsCustomizer(location)
      else null

  }
}