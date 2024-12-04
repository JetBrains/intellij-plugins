// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web

import com.intellij.html.webSymbols.hasOnlyStandardHtmlSymbolsOrExtensions
import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider.withTypeEvaluationLocation
import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.asSafely
import com.intellij.util.containers.MultiMap
import com.intellij.webSymbols.*
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_HTML
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.context.WebSymbolsContext
import com.intellij.webSymbols.query.WebSymbolMatch
import com.intellij.webSymbols.query.WebSymbolsQueryResultsCustomizer
import com.intellij.webSymbols.query.WebSymbolsQueryResultsCustomizerFactory
import com.intellij.webSymbols.utils.qualifiedKind
import com.intellij.webSymbols.utils.unwrapMatchedSymbols
import com.intellij.webSymbols.utils.withSegments
import com.intellij.webSymbols.utils.withSymbols
import com.intellij.xml.util.HtmlUtil
import org.angular2.Angular2Framework
import org.angular2.codeInsight.Angular2CodeInsightUtils
import org.angular2.codeInsight.Angular2CodeInsightUtils.wrapWithImportDeclarationModuleHandler
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity
import org.angular2.entities.Angular2Directive
import org.angular2.lang.expr.psi.Angular2TemplateBindings
import java.util.*

class Angular2WebSymbolsQueryResultsCustomizer private constructor(private val context: PsiElement) : WebSymbolsQueryResultsCustomizer {

  private val scope = Angular2DeclarationsScope(context.containingFile)
  private val svgContext = PsiTreeUtil.getParentOfType(context, XmlTag::class.java)?.namespace == HtmlUtil.SVG_NAMESPACE

  override fun apply(
    matches: List<WebSymbol>,
    strict: Boolean,
    qualifiedName: WebSymbolQualifiedName,
  ): List<WebSymbol> =
    when {
      kinds.contains(qualifiedName.qualifiedKind) ->
        withTypeEvaluationLocation(context) {
          if (strict)
            matches.filterOutOfScopeOrErrorSymbols(scope)
          else
            matches.wrapWithScopedSymbols(scope)
        }
      qualifiedName.namespace == NAMESPACE_HTML ->
        matches
          .filter { !it.extension }
          .filterByNearestProximity()
          .filterOutSelectorSymbolsIfNeeded()
          .plus(matches.filter { it.extension })
      else -> matches
    }

  override fun apply(
    item: WebSymbolCodeCompletionItem,
    qualifiedKind: WebSymbolQualifiedKind,
  ): WebSymbolCodeCompletionItem? {
    // In svg context, only standard SVG elements, ng-container and ng-template works in the browser,
    // remove everything else from completion
    if (svgContext
        && qualifiedKind == WebSymbol.HTML_ELEMENTS
        && item.name !in svgAllowedElements)
      return null
    val symbol = item.symbol
    if (symbol == null || !kinds.contains(qualifiedKind)) return item
    val directives = symbol.unwrapMatchedSymbols()
      .mapNotNull { it.properties[PROP_SYMBOL_DIRECTIVE]?.asSafely<Angular2Directive>() }
      .toList()
    return if (symbol.properties[PROP_ERROR_SYMBOL] == true) {
      null
    }
    else if (directives.isNotEmpty()) {
      withTypeEvaluationLocation(context) {
        val proximity = scope.getDeclarationsProximity(directives)
        if (proximity == DeclarationProximity.NOT_REACHABLE) {
          null
        }
        else {
          wrapWithImportDeclarationModuleHandler(
            Angular2CodeInsightUtils.decorateCodeCompletionItem(item, directives, proximity, scope),
            when (qualifiedKind) {
              NG_DIRECTIVE_ELEMENT_SELECTORS -> XmlTag::class.java
              NG_STRUCTURAL_DIRECTIVES -> Angular2TemplateBindings::class.java
              else -> XmlAttribute::class.java
            })
        }
      }
    }
    else item
  }

  private fun List<WebSymbol>.wrapWithScopedSymbols(scope: Angular2DeclarationsScope): List<WebSymbol> {
    val proximityMap = groupBy {
      val directive = it.properties[PROP_SYMBOL_DIRECTIVE] as? Angular2Directive
      if (directive != null)
        scope.getDeclarationProximity(directive)
      else if (it.properties[PROP_ERROR_SYMBOL] == true)
        DeclarationProximity.NOT_REACHABLE
      else
        DeclarationProximity.IN_SCOPE
    }
    return DeclarationProximity.entries.firstNotNullOfOrNull { proximity ->
      proximityMap[proximity]?.takeIf { it.isNotEmpty() }?.map { Angular2ScopedSymbol.create(it, proximity) }
    } ?: emptyList()
  }

  private fun List<WebSymbol>.filterOutOfScopeOrErrorSymbols(scope: Angular2DeclarationsScope): List<WebSymbol> =
    filter { symbol ->
      symbol.properties[PROP_SYMBOL_DIRECTIVE].asSafely<Angular2Directive>()?.let { scope.contains(it) } != false
      && symbol.properties[PROP_ERROR_SYMBOL] != true
    }

  private fun List<WebSymbol>.filterByNearestProximity(): List<WebSymbol> {
    if (size <= 1) return this
    val proximityMap = groupBy { match ->
      match.properties[PROP_SCOPE_PROXIMITY].asSafely<DeclarationProximity>()
      ?: DeclarationProximity.NOT_REACHABLE.takeIf { match.properties[PROP_ERROR_SYMBOL] == true }
      ?: DeclarationProximity.IN_SCOPE
    }
    return DeclarationProximity.entries.firstNotNullOfOrNull { proximity ->
      proximityMap[proximity]?.takeIf { it.isNotEmpty() }
    } ?: emptyList()
  }

  private fun List<WebSymbol>.filterOutSelectorSymbolsIfNeeded(): List<WebSymbol> =
    if (size <= 1)
      this
    // If we have an HTML symbol, filter out all ng selectors
    else if (any { it.hasOnlyStandardHtmlSymbolsOrExtensions() })
      filter { !it.hasSelectorSymbols() }
    else {
      // If no HTML symbols, group by directive and prefer non-selector symbols for a particular directive.
      // We want to avoid resolving to selectors if there is an input with the same name,
      // but only within the same directive. So we need some complicated logic here to achieve that.
      val candidates = MultiMap<Angular2Directive, WebSymbol>()
      flatMap {
        if (it is WebSymbolMatch)
          it.nameSegments.flatMap { it.symbols }
        else
          listOf(it)
      }.forEach { symbol ->
        (symbol.properties[PROP_SYMBOL_DIRECTIVE] as? Angular2Directive)?.let { candidates.putValue(it, symbol) }
      }

      val filteredSymbols = candidates.toHashMap()
        .flatMap { (_, list) ->
          list
            ?.takeIf { it.size > 1 }
            ?.groupBy { it.hasSelectorSymbols() }[false]
            ?.takeIf { it.isNotEmpty() }
          ?: list
        }

      // We need to remap matches within WebSymbolMatch, as the selector symbols are nested at this point
      mapNotNull {
        if (it is WebSymbolMatch) {
          val newSegments = it.nameSegments.mapNotNull { segment ->
            val newSymbols = segment.symbols.mapNotNull {
              it.takeIf {
                filteredSymbols.contains(it) || it.properties[PROP_SYMBOL_DIRECTIVE] == null
              }
            }
            when {
              // Remove the match completely if we filtered out all the symbols
              newSymbols.isEmpty() -> null
              newSymbols != segment.symbols -> segment.withSymbols(newSymbols)
              else -> segment
            }
          }
          when {
            newSegments.size != it.nameSegments.size -> null
            newSegments != it.nameSegments -> it.withSegments(newSegments)
            else -> it
          }
        }
        else
          it.takeIf {
            filteredSymbols.contains(it) || it.properties[PROP_SYMBOL_DIRECTIVE] == null
          }
      }
    }

  private fun WebSymbol.hasSelectorSymbols() =
    unwrapMatchedSymbols()
      .any { it.qualifiedKind == NG_DIRECTIVE_ATTRIBUTE_SELECTORS || it.qualifiedKind == NG_DIRECTIVE_ELEMENT_SELECTORS }

  companion object {
    private val svgAllowedElements = setOf(ELEMENT_NG_CONTAINER, ELEMENT_NG_TEMPLATE)

    private val kinds = setOf(NG_STRUCTURAL_DIRECTIVES,
                              NG_DIRECTIVE_ONE_TIME_BINDINGS, NG_DIRECTIVE_ATTRIBUTES,
                              NG_DIRECTIVE_INPUTS, NG_DIRECTIVE_OUTPUTS, NG_DIRECTIVE_IN_OUTS,
                              NG_DIRECTIVE_ELEMENT_SELECTORS, NG_DIRECTIVE_ATTRIBUTE_SELECTORS)
  }

  override fun createPointer(): Pointer<out WebSymbolsQueryResultsCustomizer> {
    val contextPtr = context.createSmartPointer()
    return Pointer {
      contextPtr.dereference()?.let { Angular2WebSymbolsQueryResultsCustomizer(it) }
    }
  }

  override fun equals(other: Any?): Boolean =
    other === this ||
    other is Angular2WebSymbolsQueryResultsCustomizer
    && other.context == context

  override fun hashCode(): Int =
    context.hashCode()

  override fun getModificationCount(): Long =
    PsiModificationTracker.getInstance(context.project).modificationCount

  class Factory : WebSymbolsQueryResultsCustomizerFactory {
    override fun create(location: PsiElement, context: WebSymbolsContext): WebSymbolsQueryResultsCustomizer? =
      if (context.framework == Angular2Framework.ID && location.containingFile != null)
        Angular2WebSymbolsQueryResultsCustomizer(location)
      else null

  }

  private open class Angular2ScopedSymbol private constructor(
    symbol: WebSymbol,
    private val scopeProximity: DeclarationProximity,
  ) : WebSymbolDelegate<WebSymbol>(symbol) {

    companion object {

      @JvmStatic
      fun create(
        symbol: WebSymbol,
        scopeProximity: DeclarationProximity,
      ): Angular2ScopedSymbol =
        when (symbol) {
          is PsiSourcedWebSymbol -> Angular2PsiSourcedScopedSymbol(symbol, scopeProximity)
          else -> Angular2ScopedSymbol(symbol, scopeProximity)
        }

    }

    override val priority: WebSymbol.Priority?
      get() = if (scopeProximity == DeclarationProximity.IN_SCOPE || scopeProximity == DeclarationProximity.IMPORTABLE)
        super.priority
      else
        WebSymbol.Priority.LOWEST

    override fun createPointer(): Pointer<out Angular2ScopedSymbol> =
      createPointer(::Angular2ScopedSymbol)

    fun <T : Angular2ScopedSymbol> createPointer(
      create: (symbol: WebSymbol, scopeProximity: DeclarationProximity) -> T,
    ): Pointer<T> {
      val delegatePtr = delegate.createPointer()
      val scopeProximity = this.scopeProximity
      return Pointer {
        delegatePtr.dereference()?.let { create(it, scopeProximity) }
      }
    }

    override fun isEquivalentTo(symbol: Symbol): Boolean =
      this == symbol || delegate.isEquivalentTo(symbol)

    override fun equals(other: Any?): Boolean =
      other === this
      || other is Angular2ScopedSymbol
      && other.delegate == delegate
      && other.scopeProximity == scopeProximity

    override fun hashCode(): Int =
      Objects.hash(delegate, scopeProximity)

    override val properties: Map<String, Any>
      get() = super.properties + Pair(PROP_SCOPE_PROXIMITY, scopeProximity)

    private class Angular2PsiSourcedScopedSymbol(
      symbol: WebSymbol,
      scopeProximity: DeclarationProximity,
    ) : Angular2ScopedSymbol(symbol, scopeProximity), PsiSourcedWebSymbol {

      override val source: PsiElement?
        get() = (delegate as PsiSourcedWebSymbol).source

      override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
        super<Angular2ScopedSymbol>.getNavigationTargets(project)

      override val psiContext: PsiElement?
        get() = super<Angular2ScopedSymbol>.psiContext

      override fun isEquivalentTo(symbol: Symbol): Boolean =
        super<Angular2ScopedSymbol>.isEquivalentTo(symbol)

      override fun createPointer(): Pointer<Angular2PsiSourcedScopedSymbol> =
        createPointer(::Angular2PsiSourcedScopedSymbol)
    }
  }
}