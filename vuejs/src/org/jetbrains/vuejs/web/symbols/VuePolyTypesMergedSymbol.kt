// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.javascript.polySymbols.jsType
import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer
import com.intellij.openapi.util.text.StringUtil
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.polySymbols.*
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.documentation.PolySymbolDocumentation
import com.intellij.polySymbols.documentation.PolySymbolDocumentationTarget
import com.intellij.polySymbols.documentation.PolySymbolWithDocumentation
import com.intellij.polySymbols.query.PolySymbolsCodeCompletionQueryParams
import com.intellij.polySymbols.query.PolySymbolsListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolsNameMatchQueryParams
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.polySymbols.utils.PsiSourcedPolySymbolDelegate
import com.intellij.polySymbols.utils.coalesceApiStatus
import com.intellij.polySymbols.utils.merge
import com.intellij.polySymbols.webTypes.WebTypesSymbol.Companion.PROP_HTML_ATTRIBUTE_VALUE
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.util.asSafely
import com.intellij.util.containers.Stack
import org.jetbrains.annotations.Nls
import org.jetbrains.vuejs.codeInsight.toAsset
import javax.swing.Icon

class VuePolyTypesMergedSymbol(
  override val name: String,
  override val delegate: PsiSourcedPolySymbol,
  val webTypesSymbols: Collection<PolySymbol>,
) : PsiSourcedPolySymbolDelegate<PsiSourcedPolySymbol>,
    CompositePolySymbol, PolySymbolWithDocumentation {

  private val symbols: List<PolySymbol> = sequenceOf(delegate)
    .plus(webTypesSymbols).toList()

  private val originalName: String?
    get() = symbols.getOrNull(1)
      ?.name
      ?.takeIf { toAsset(it) != toAsset(name) }

  override val origin: PolySymbolOrigin
    get() = symbols.getOrNull(1)?.origin ?: super.origin

  override fun getModificationCount(): Long =
    symbols.sumOf { it.modificationCount }

  override val nameSegments: List<PolySymbolNameSegment>
    get() = listOf(PolySymbolNameSegment.create(
      0, name.length, symbols
    ))

  override val description: String?
    get() = symbols.firstNotNullOfOrNull {
      it.asSafely<PolySymbolWithDocumentation>()?.description
    }

  override val descriptionSections: Map<String, String>
    get() = symbols.asSequence()
      .flatMap { it.asSafely<PolySymbolWithDocumentation>()?.descriptionSections?.entries ?: emptySet() }
      .distinctBy { it.key }
      .associateBy({ it.key }, { it.value })

  override val docUrl: String?
    get() = symbols.firstNotNullOfOrNull { it.asSafely<PolySymbolWithDocumentation>()?.docUrl }

  override val icon: Icon?
    get() = symbols.firstNotNullOfOrNull { it.icon }

  override val apiStatus: PolySymbolApiStatus
    get() = coalesceApiStatus(symbols) { it.apiStatus }

  override val required: Boolean?
    get() = symbols.firstNotNullOfOrNull { it.required }

  override val defaultValue: String?
    get() = symbols.firstNotNullOfOrNull { it.asSafely<PolySymbolWithDocumentation>()?.defaultValue }

  override val priority: PolySymbol.Priority?
    get() = symbols.asSequence().mapNotNull { it.priority }.maxOrNull()

  override val type: JSType?
    get() = symbols.firstNotNullOfOrNull { it.jsType }

  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PROP_HTML_ATTRIBUTE_VALUE -> property.tryCast(symbols.asSequence().map { it[PROP_HTML_ATTRIBUTE_VALUE] }.merge())
      else -> symbols.asSequence().mapNotNull { it[property] }.firstOrNull()
    }


  override val queryScope: List<PolySymbolsScope>
    get() = listOf(this)

  override fun createDocumentation(location: PsiElement?): PolySymbolDocumentation =
    PolySymbolDocumentation.create(this, location).let { doc ->
      originalName
        ?.let { doc.withDefinition(StringUtil.escapeXmlEntities(it) + " as " + doc.definition) }
      ?: doc
    }

  override fun getDocumentationTarget(location: PsiElement?): DocumentationTarget =
    VueMergedSymbolDocumentationTarget(this, location, originalName ?: name)

  override fun getMatchingSymbols(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolsNameMatchQueryParams,
    scope: Stack<PolySymbolsScope>,
  ): List<PolySymbol> =
    symbols
      .flatMap {
        it.getMatchingSymbols(qualifiedName, params, scope)
      }
      .let { list ->
        val psiSourcedPolySymbol = list.firstNotNullOfOrNull { it as? PsiSourcedPolySymbol }
        if (psiSourcedPolySymbol != null) {
          listOf(VuePolyTypesMergedSymbol(psiSourcedPolySymbol.name, psiSourcedPolySymbol, list))
        }
        else {
          list
        }
      }

  override fun getSymbols(
    qualifiedKind: PolySymbolQualifiedKind,
    params: PolySymbolsListSymbolsQueryParams,
    scope: Stack<PolySymbolsScope>,
  ): List<PolySymbolsScope> =
    symbols
      .flatMap {
        it.getSymbols(qualifiedKind, params, scope)
      }
      .takeIf { it.isNotEmpty() }
      ?.let { list ->
        val containers = mutableListOf<PolySymbolsScope>()
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
            is PolySymbol -> polySymbols.add(item)
            else -> containers.add(item)
          }
        }
        if (psiSourcedPolySymbol != null) {
          containers.add(VuePolyTypesMergedSymbol(psiSourcedPolySymbol.name, psiSourcedPolySymbol, polySymbols))
        }
        else {
          containers.addAll(polySymbols)
        }
        containers
      }
    ?: emptyList()

  override fun getCodeCompletions(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolsCodeCompletionQueryParams,
    scope: Stack<PolySymbolsScope>,
  ): List<PolySymbolCodeCompletionItem> =
    symbols.asSequence()
      .flatMap { it.getCodeCompletions(qualifiedName, params, scope) }
      .groupBy { it.name }
      .values
      .map { items ->
        if (items.size == 1)
          items[0]
        else {
          var psiSourcedPolySymbol: PsiSourcedPolySymbol? = null
          val symbols = mutableListOf<PolySymbol>()
          items.asSequence().mapNotNull { it.symbol }.forEach {
            if (it is PsiSourcedPolySymbol && psiSourcedPolySymbol == null)
              psiSourcedPolySymbol = it
            else symbols.add(it)
          }
          psiSourcedPolySymbol?.let {
            items[0].withSymbol(VuePolyTypesMergedSymbol(it.name, it, symbols))
          } ?: items[0]
        }
      }

  override fun createPointer(): Pointer<out VuePolyTypesMergedSymbol> {
    val pointers = symbols.map { it.createPointer() }
    val matchedName = name
    return Pointer {
      val symbols = pointers.map { it.dereference() ?: return@Pointer null }
      VuePolyTypesMergedSymbol(matchedName,
                               symbols[0] as? PsiSourcedPolySymbol ?: return@Pointer null,
                               symbols.subList(1, symbols.size))
    }
  }

  class VueMergedSymbolDocumentationTarget(
    override val symbol: VuePolyTypesMergedSymbol,
    override val location: PsiElement?,
    @Nls val displayName: String,
  ) : PolySymbolDocumentationTarget {

    override fun computePresentation(): TargetPresentation {
      return TargetPresentation.builder(displayName)
        .icon(symbol.icon)
        .presentation()
    }

    override fun computeDocumentation(): DocumentationResult? =
      symbol.createDocumentation(location)
        .takeIf { it.isNotEmpty() }
        ?.build(symbol.origin)


    override fun createPointer(): Pointer<out DocumentationTarget> {
      val pointer = symbol.createPointer()
      val locationPtr = location?.createSmartPointer()
      val displayName = this.displayName
      return Pointer<DocumentationTarget> {
        pointer.dereference()?.let { VueMergedSymbolDocumentationTarget(it, locationPtr?.dereference(), displayName) }
      }
    }
  }
}