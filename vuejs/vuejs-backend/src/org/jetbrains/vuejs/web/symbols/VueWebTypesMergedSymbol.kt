// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.model.Pointer
import com.intellij.openapi.util.text.StringUtil
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.polySymbols.*
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.documentation.PolySymbolDocumentation
import com.intellij.polySymbols.documentation.PolySymbolDocumentationProvider
import com.intellij.polySymbols.documentation.PolySymbolDocumentationTarget
import com.intellij.polySymbols.html.PROP_HTML_ATTRIBUTE_VALUE
import com.intellij.polySymbols.js.jsType
import com.intellij.polySymbols.js.types.PROP_JS_TYPE
import com.intellij.polySymbols.query.*
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.polySymbols.utils.PsiSourcedPolySymbolDelegate
import com.intellij.polySymbols.utils.coalesceApiStatus
import com.intellij.polySymbols.utils.merge
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.codeInsight.toAsset
import javax.swing.Icon

class VueWebTypesMergedSymbol(
  override val name: String,
  override val delegate: PsiSourcedPolySymbol,
  val webTypesSymbols: Collection<PolySymbol>,
) : PsiSourcedPolySymbolDelegate<PsiSourcedPolySymbol>,
    CompositePolySymbol, PolySymbolScope {

  private val symbols: List<PolySymbol> = sequenceOf(delegate)
    .plus(webTypesSymbols).toList()

  private val originalName: String?
    get() = symbols.getOrNull(1)
      ?.name
      ?.takeIf { toAsset(it) != toAsset(name) }

  override val origin: PolySymbolOrigin
    get() = symbols.getOrNull(1)?.origin ?: super.origin

  override fun getModificationCount(): Long =
    symbols.sumOf { (it as? PolySymbolScope)?.modificationCount ?: 0 }

  override val nameSegments: List<PolySymbolNameSegment>
    get() = listOf(PolySymbolNameSegment.create(
      0, name.length, symbols
    ))

  override fun getDocumentationTarget(location: PsiElement?): DocumentationTarget =
    PolySymbolDocumentationTarget.create(this, location, VueMergedSymbolDocumentationProvider)

  override val icon: Icon?
    get() = symbols.firstNotNullOfOrNull { it.icon }

  override val apiStatus: PolySymbolApiStatus
    get() = coalesceApiStatus(symbols) { it.apiStatus }


  override val priority: PolySymbol.Priority?
    get() = symbols.asSequence().mapNotNull { it.priority }.maxOrNull()

  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PROP_HTML_ATTRIBUTE_VALUE -> property.tryCast(symbols.asSequence().map { it[PROP_HTML_ATTRIBUTE_VALUE] }.merge())
      PROP_JS_TYPE -> property.tryCast(symbols.firstNotNullOfOrNull { it.jsType })
      else -> symbols.asSequence().mapNotNull { it[property] }.firstOrNull()
    }

  override val modifiers: Set<PolySymbolModifier>
    get() = symbols.asSequence().flatMap { it.modifiers }.toSet()

  override val queryScope: List<PolySymbolScope>
    get() = listOf(this)

  override fun getMatchingSymbols(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolNameMatchQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> =
    symbols
      .asSequence()
      .flatMap { it.queryScope }
      .flatMap { it.getMatchingSymbols(qualifiedName, params, stack) }
      .toList()
      .let { list ->
        val psiSourcedPolySymbol = list.firstNotNullOfOrNull { it as? PsiSourcedPolySymbol }
        if (psiSourcedPolySymbol != null) {
          listOf(VueWebTypesMergedSymbol(psiSourcedPolySymbol.name, psiSourcedPolySymbol, list))
        }
        else {
          list
        }
      }

  override fun getSymbols(
    kind: PolySymbolKind,
    params: PolySymbolListSymbolsQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> =
    symbols
      .asSequence()
      .flatMap { it.queryScope }
      .flatMap { it.getSymbols(kind, params, stack) }
      .toList()
      .takeIf { it.isNotEmpty() }
      ?.groupBy { it.name }
      ?.mapValues { (_, list) ->
        val containers = mutableListOf<PolySymbol>()
        var psiSourcedPolySymbol: PsiSourcedPolySymbol? = null
        val polySymbols = mutableListOf<PolySymbol>()
        for (item in list) {
          when (item) {
            is PsiSourcedPolySymbol -> {
              if (psiSourcedPolySymbol == null) {
                psiSourcedPolySymbol = item
              }
              else polySymbols.add(item)
            }
            else -> polySymbols.add(item)
          }
        }
        if (psiSourcedPolySymbol != null) {
          containers.add(VueWebTypesMergedSymbol(psiSourcedPolySymbol.name, psiSourcedPolySymbol, polySymbols))
        }
        else {
          containers.addAll(polySymbols)
        }
        containers
      }
      ?.flatMap { it.value }
    ?: emptyList()

  override fun getCodeCompletions(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolCodeCompletionQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbolCodeCompletionItem> =
    symbols.asSequence()
      .flatMap { it.queryScope }
      .flatMap { it.getCodeCompletions(qualifiedName, params, stack) }
      .groupBy { it.name }
      .values
      .map { items ->
        if (items.size == 1)
          items[0]
        else {
          var psiSourcedPolySymbol: PsiSourcedPolySymbol? = null
          val symbols = mutableListOf<PolySymbol>()
          items.asSequence().mapNotNull { it.symbol }.distinct().forEach {
            if (it is PsiSourcedPolySymbol && psiSourcedPolySymbol == null)
              psiSourcedPolySymbol = it
            else symbols.add(it)
          }
          psiSourcedPolySymbol?.let {
            items[0].withSymbol(VueWebTypesMergedSymbol(it.name, it, symbols))
          } ?: items[0]
        }
      }

  override fun createPointer(): Pointer<out VueWebTypesMergedSymbol> {
    val pointers = symbols.map { it.createPointer() }
    val matchedName = name
    return Pointer {
      val symbols = pointers.map { it.dereference() ?: return@Pointer null }
      VueWebTypesMergedSymbol(matchedName,
                              symbols[0] as? PsiSourcedPolySymbol ?: return@Pointer null,
                              symbols.subList(1, symbols.size))
    }
  }

  object VueMergedSymbolDocumentationProvider : PolySymbolDocumentationProvider<VueWebTypesMergedSymbol> {

    override fun computePresentation(symbol: VueWebTypesMergedSymbol): TargetPresentation =
      TargetPresentation.builder(symbol.originalName ?: symbol.name)
        .icon(symbol.icon)
        .presentation()

    override fun createDocumentation(symbol: VueWebTypesMergedSymbol, location: PsiElement?): PolySymbolDocumentation {
      val symbolsDocs = symbol.symbols
        .mapNotNull { (it.getDocumentationTarget(location) as? PolySymbolDocumentationTarget)?.documentation }

      return PolySymbolDocumentation.builder(symbol, location)
        .description(symbolsDocs.firstNotNullOfOrNull { it.description })
        .descriptionSections(
          symbolsDocs.asSequence()
            .flatMap { it.descriptionSections.entries }
            .distinctBy { it.key }
            .associateBy({ it.key }, { it.value })
        )
        .docUrl(symbolsDocs.firstNotNullOfOrNull { it.docUrl })
        .defaultValue(symbolsDocs.firstNotNullOfOrNull { it.defaultValue })
        .also { builder ->
          symbol.originalName?.let { builder.definition(StringUtil.escapeXmlEntities(it) + " as " + builder.definition) }
        }
        .library(symbolsDocs.firstNotNullOfOrNull { it.library })
        .build()
    }
  }
}