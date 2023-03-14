// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web

import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.navigation.NavigationTarget
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.refactoring.rename.api.RenameTarget
import com.intellij.refactoring.rename.symbol.RenameableSymbol
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.asSafely
import com.intellij.webSymbols.*
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_HTML
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_JS
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.context.WebSymbolsContext
import com.intellij.webSymbols.query.WebSymbolsQueryResultsCustomizer
import com.intellij.webSymbols.query.WebSymbolsQueryResultsCustomizerFactory
import com.intellij.webSymbols.utils.psiModificationCount
import com.intellij.webSymbols.utils.unwrapMatchedSymbols
import com.intellij.xml.util.HtmlUtil
import org.angular2.Angular2Framework
import org.angular2.codeInsight.Angular2CodeInsightUtils
import org.angular2.codeInsight.Angular2CodeInsightUtils.wrapWithImportDeclarationModuleHandler
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity
import org.angular2.entities.Angular2Directive
import org.angular2.lang.expr.psi.Angular2TemplateBindings
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_ATTRIBUTES
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_ATTRIBUTE_SELECTORS
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_ELEMENT_SELECTORS
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_INPUTS
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_IN_OUTS
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_ONE_TIME_BINDINGS
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_OUTPUTS
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_STRUCTURAL_DIRECTIVES
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.PROP_ERROR_SYMBOL
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.PROP_SCOPE_PROXIMITY
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.PROP_SYMBOL_DIRECTIVE
import java.util.*

class Angular2WebSymbolsQueryResultsCustomizer private constructor(private val context: PsiElement) : WebSymbolsQueryResultsCustomizer {

  private val scope = Angular2DeclarationsScope(context.containingFile)
  private val svgContext = PsiTreeUtil.getParentOfType(context, XmlTag::class.java)?.namespace == HtmlUtil.SVG_NAMESPACE

  override fun apply(matches: List<WebSymbol>,
                     strict: Boolean,
                     namespace: SymbolNamespace?,
                     kind: SymbolKind,
                     name: String?): List<WebSymbol> =
    if (namespace == NAMESPACE_JS && kinds.contains(kind)) {
      if (strict) {
        matches.filter { symbol ->
          symbol.properties[PROP_SYMBOL_DIRECTIVE].asSafely<Angular2Directive>()?.let { scope.contains(it) } != false
          && symbol.properties[PROP_ERROR_SYMBOL] != true
        }
      }
      else {
        val byName = if (name == null)
          matches.groupBy { it.name }
        else
          mapOf(Pair(name, matches))

        byName.flatMap { (_, list) ->
          val proximityMap = list.groupBy {
            val directive = it.properties[PROP_SYMBOL_DIRECTIVE] as? Angular2Directive
            if (directive != null)
              scope.getDeclarationProximity(directive)
            else if (it.properties[PROP_ERROR_SYMBOL] == true)
              DeclarationProximity.NOT_REACHABLE
            else
              DeclarationProximity.IN_SCOPE
          }
          DeclarationProximity.values().firstNotNullOfOrNull { proximity ->
            proximityMap[proximity]?.takeIf { it.isNotEmpty() }?.map { Angular2ScopedSymbol.create(it, proximity) }
          }
          ?: emptyList()
        }
      }
    }
    else matches

  override fun apply(item: WebSymbolCodeCompletionItem,
                     namespace: SymbolNamespace?,
                     kind: SymbolKind): WebSymbolCodeCompletionItem? {
    // In svg context, only standard SVG elements, ng-container and ng-template works in the browser,
    // remove everything else from completion
    if (svgContext
        && namespace == NAMESPACE_HTML
        && kind == WebSymbol.KIND_HTML_ELEMENTS
        && item.name !in svgAllowedElements)
      return null
    val symbol = item.symbol
    if (symbol == null || namespace != NAMESPACE_JS || !kinds.contains(kind)) return item
    val directives = symbol.unwrapMatchedSymbols()
      .mapNotNull { it.properties[PROP_SYMBOL_DIRECTIVE]?.asSafely<Angular2Directive>() }
      .toList()
    return if (symbol.properties[PROP_ERROR_SYMBOL] == true) {
      null
    }
    else if (directives.isNotEmpty()) {
      val proximity = scope.getDeclarationsProximity(directives)
      if (proximity == DeclarationProximity.NOT_REACHABLE) {
        null
      }
      else {
        wrapWithImportDeclarationModuleHandler(
          Angular2CodeInsightUtils.decorateCodeCompletionItem(item, directives, proximity, scope),
          when (kind) {
            KIND_NG_DIRECTIVE_ELEMENT_SELECTORS -> XmlTag::class.java
            KIND_NG_STRUCTURAL_DIRECTIVES -> Angular2TemplateBindings::class.java
            else -> XmlAttribute::class.java
          })
      }
    }
    else item
  }

  companion object {
    private val svgAllowedElements = setOf("ng-container", "ng-template")

    private val kinds = setOf(KIND_NG_STRUCTURAL_DIRECTIVES,
                              KIND_NG_DIRECTIVE_ONE_TIME_BINDINGS, KIND_NG_DIRECTIVE_ATTRIBUTES,
                              KIND_NG_DIRECTIVE_INPUTS, KIND_NG_DIRECTIVE_OUTPUTS, KIND_NG_DIRECTIVE_IN_OUTS,
                              KIND_NG_DIRECTIVE_ELEMENT_SELECTORS, KIND_NG_DIRECTIVE_ATTRIBUTE_SELECTORS)
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
    context.project.psiModificationCount

  class Factory : WebSymbolsQueryResultsCustomizerFactory {
    override fun create(location: PsiElement, context: WebSymbolsContext): WebSymbolsQueryResultsCustomizer? =
      if (context.framework == Angular2Framework.ID && location.containingFile != null)
        Angular2WebSymbolsQueryResultsCustomizer(location)
      else null

  }

  private open class Angular2ScopedSymbol private constructor(symbol: WebSymbol,
                                                              private val scopeProximity: DeclarationProximity)
    : WebSymbolDelegate<WebSymbol>(symbol) {

    companion object {

      @JvmStatic
      fun create(symbol: WebSymbol,
                 scopeProximity: DeclarationProximity): Angular2ScopedSymbol =
        when (symbol) {
          is PsiSourcedWebSymbol -> Angular2PsiSourcedScopedSymbol(symbol, scopeProximity)
          is RenameableSymbol,
          is RenameTarget -> Angular2RenameableScopedSymbol(symbol, scopeProximity)
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

    protected fun <T : Angular2ScopedSymbol> createPointer(
      create: (symbol: WebSymbol, scopeProximity: DeclarationProximity) -> T
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


    private class Angular2RenameableScopedSymbol(symbol: WebSymbol,
                                                 scopeProximity: DeclarationProximity)
      : Angular2ScopedSymbol(symbol, scopeProximity), RenameableSymbol {
      override val renameTarget: RenameTarget
        get() = renameTargetFromDelegate()

      override fun createPointer(): Pointer<Angular2RenameableScopedSymbol> =
        createPointer(::Angular2RenameableScopedSymbol)
    }

    private class Angular2PsiSourcedScopedSymbol(symbol: WebSymbol,
                                                 scopeProximity: DeclarationProximity)
      : Angular2ScopedSymbol(symbol, scopeProximity), PsiSourcedWebSymbol {

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