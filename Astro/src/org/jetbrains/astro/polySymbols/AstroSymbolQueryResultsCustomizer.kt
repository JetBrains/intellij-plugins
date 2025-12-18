// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.polySymbols

import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolQualifiedName
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.context.PolyContext
import com.intellij.polySymbols.query.PolySymbolQueryResultsCustomizer
import com.intellij.polySymbols.query.PolySymbolQueryResultsCustomizerFactory
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.util.asSafely
import com.intellij.xml.util.Html5TagAndAttributeNamesProvider
import org.jetbrains.astro.AstroFramework
import org.jetbrains.astro.codeInsight.completion.AstroImportInsertHandler
import org.jetbrains.astro.lang.AstroFileImpl
import org.jetbrains.astro.polySymbols.symbols.AstroComponent

class AstroSymbolQueryResultsCustomizer(private val context: PsiElement) : PolySymbolQueryResultsCustomizer {

  override fun apply(
    matches: List<PolySymbol>,
    strict: Boolean,
    qualifiedName: PolySymbolQualifiedName,
  ): List<PolySymbol> =
    if (qualifiedName.kind != ASTRO_COMPONENTS)
      matches
    else if (isHtmlTagName(qualifiedName.name))
      emptyList()
    else
      matches.filter { symbol ->
        symbol.asSafely<AstroComponent>()?.source != context.containingFile.originalFile
        && (!strict || symbol[PROP_ASTRO_PROXIMITY].let {
          it == null || it == AstroProximity.LOCAL
        })
      }

  override fun apply(item: PolySymbolCodeCompletionItem, kind: PolySymbolKind): PolySymbolCodeCompletionItem? {
    if (kind == ASTRO_COMPONENTS) {
      if (isHtmlTagName(item.name)) return null
      val proximity = item.symbol?.get(PROP_ASTRO_PROXIMITY)
      val element = (item.symbol as? PsiSourcedPolySymbol)?.source
      if (proximity == AstroProximity.OUT_OF_SCOPE && element is AstroFileImpl) {
        return if (element != context.containingFile.originalFile)
          item.withInsertHandlerAdded(AstroImportInsertHandler, PolySymbol.Priority.LOWEST)
        else null
      }
    }
    return item
  }

  override fun createPointer(): Pointer<out PolySymbolQueryResultsCustomizer> {
    val contextPtr = context.createSmartPointer()
    return Pointer {
      contextPtr.dereference()?.let { AstroSymbolQueryResultsCustomizer(it) }
    }
  }

  override fun getModificationCount(): Long = 0

  override fun equals(other: Any?): Boolean =
    other is AstroSymbolQueryResultsCustomizer
    && other.context == context

  override fun hashCode(): Int =
    context.hashCode()

  private fun isHtmlTagName(name: String?) =
    name?.getOrNull(0)?.isLowerCase() == true
    && Html5TagAndAttributeNamesProvider.getTags(Html5TagAndAttributeNamesProvider.Namespace.HTML, false).contains(name)

  class Factory : PolySymbolQueryResultsCustomizerFactory {
    override fun create(location: PsiElement, context: PolyContext): PolySymbolQueryResultsCustomizer? =
      if (context.framework == AstroFramework.ID)
        AstroSymbolQueryResultsCustomizer(location)
      else null

  }
}