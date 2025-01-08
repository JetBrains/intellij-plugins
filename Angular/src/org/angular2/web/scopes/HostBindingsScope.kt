package org.angular2.web.scopes

import com.intellij.html.webSymbols.WebSymbolsHtmlQueryHelper
import com.intellij.html.webSymbols.WebSymbolsHtmlQueryHelper.getStandardHtmlElementSymbolsScope
import com.intellij.html.webSymbols.hasOnlyStandardHtmlSymbolsOrExtensions
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.model.Pointer
import com.intellij.openapi.util.RecursionManager
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.applyIf
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.WebSymbolQualifiedName
import com.intellij.webSymbols.WebSymbolsScope
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.query.*
import com.intellij.webSymbols.utils.withMatchedKind
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.web.PROP_HOST_BINDING

class HostBindingsScope(private val decorator: ES6Decorator) : WebSymbolsScope {

  private val subQuery by lazy(LazyThreadSafetyMode.PUBLICATION) {
    getCachedSubQueryExecutorAndScope().first
  }
  private val additionalScope by lazy(LazyThreadSafetyMode.PUBLICATION) {
    getCachedSubQueryExecutorAndScope().second
  }

  override fun getCodeCompletions(qualifiedName: WebSymbolQualifiedName, params: WebSymbolsCodeCompletionQueryParams, scope: Stack<WebSymbolsScope>): List<WebSymbolCodeCompletionItem> {
    if (qualifiedName.qualifiedKind != WebSymbol.JS_PROPERTIES) return emptyList()
    var result: List<WebSymbolCodeCompletionItem> = emptyList()
    RecursionManager.runInNewContext {
      result = subQuery.runCodeCompletionQuery(WebSymbol.HTML_ATTRIBUTES, qualifiedName.name, params.position, params.virtualSymbols, additionalScope)
        .filter { acceptSymbol(it.symbol) }
    }
    return result
  }

  override fun getMatchingSymbols(qualifiedName: WebSymbolQualifiedName, params: WebSymbolsNameMatchQueryParams, scope: Stack<WebSymbolsScope>): List<WebSymbol> {
    if (qualifiedName.qualifiedKind != WebSymbol.JS_PROPERTIES) return emptyList()
    var result: List<WebSymbol> = emptyList()
    RecursionManager.runInNewContext {
      result = subQuery.runNameMatchQuery(WebSymbol.HTML_ATTRIBUTES.withName(qualifiedName.name), params.virtualSymbols, params.abstractSymbols, params.strictScope, additionalScope)
        .filter { acceptSymbol(it) }
        .map { it.withMatchedKind(qualifiedName.qualifiedKind) }
    }
    return result
  }

  override fun getSymbols(qualifiedKind: WebSymbolQualifiedKind, params: WebSymbolsListSymbolsQueryParams, scope: Stack<WebSymbolsScope>): List<WebSymbolsScope> {
    if (qualifiedKind != WebSymbol.JS_PROPERTIES) return emptyList()
    var result: List<WebSymbol> = emptyList()
    RecursionManager.runInNewContext {
      result = subQuery.runListSymbolsQuery(WebSymbol.HTML_ATTRIBUTES, params.expandPatterns, params.virtualSymbols, params.abstractSymbols, params.strictScope, additionalScope)
        .filter { acceptSymbol(it) }
        .applyIf(params.expandPatterns) { map { it.withMatchedKind(qualifiedKind) } }
    }
    return result
  }

  override fun createPointer(): Pointer<HostBindingsScope> {
    val decoratorPtr = decorator.createSmartPointer()
    return Pointer {
      decoratorPtr.dereference()?.let { HostBindingsScope(it) }
    }
  }

  override fun getModificationCount(): Long = 0

  override fun equals(other: Any?): Boolean =
    other === this || (other is HostBindingsScope && decorator == other.decorator)

  override fun hashCode(): Int =
    decorator.hashCode()

  private fun acceptSymbol(symbol: WebSymbol?): Boolean =
    symbol == null ||
    (symbol.properties[PROP_HOST_BINDING] != false && (!symbol.name.startsWith("on") || !symbol.hasOnlyStandardHtmlSymbolsOrExtensions()))

  private fun getCachedSubQueryExecutorAndScope(): Pair<WebSymbolsQueryExecutor, List<WebSymbolsScope>> {
    val decorator = decorator
    return CachedValuesManager.getCachedValue(decorator) {
      val executor = WebSymbolsQueryExecutorFactory.create(decorator)
      val file = decorator.containingFile
      val scope = mutableSetOf<WebSymbolsScope>(
        StandardPropertyAndEventsScope(file),
        DirectiveElementSelectorsScope(file),
        DirectiveAttributeSelectorsScope(file),
        getStandardHtmlElementSymbolsScope(file.project)
      )
      val elementNames = Angular2EntitiesProvider.getDirective(decorator)?.selector?.simpleSelectors
        ?.mapNotNull { it.elementName?.trim()?.takeIf { it.isNotEmpty() && it != "*" } }
      if (!elementNames.isNullOrEmpty()) {
        elementNames.forEach {
          scope.add(WebSymbolsHtmlQueryHelper.getStandardHtmlAttributeSymbolsScopeForTag(file.project, it))
        }
        val scopeList = scope.toList()
        elementNames.flatMapTo(scope) { executor.runNameMatchQuery(WebSymbol.HTML_ELEMENTS.withName(it), additionalScope = scopeList) }
        elementNames.mapTo(scope) { MatchedDirectivesScope.createFor(decorator, it) }
      }
      else {
        scope.add(WebSymbolsHtmlQueryHelper.getStandardHtmlAttributeSymbolsScopeForTag(file.project, "div"))
        executor.runNameMatchQuery(WebSymbol.HTML_ELEMENTS.withName("div"), additionalScope = scope.toList())
          .forEach { scope.add(it) }
      }
      CachedValueProvider.Result.create(Pair(executor, scope.toList()), PsiModificationTracker.MODIFICATION_COUNT)
    }
  }

}