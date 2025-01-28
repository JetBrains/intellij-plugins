package org.angular2.web.scopes

import com.intellij.html.webSymbols.WebSymbolsHtmlQueryHelper
import com.intellij.html.webSymbols.WebSymbolsHtmlQueryHelper.getStandardHtmlElementSymbolsScope
import com.intellij.html.webSymbols.hasOnlyStandardHtmlSymbolsOrExtensions
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.model.Pointer
import com.intellij.psi.createSmartPointer
import com.intellij.psi.css.StylesheetFile
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.WebSymbolsScope
import com.intellij.webSymbols.css.getWebSymbolsCssScopeForTagClasses
import com.intellij.webSymbols.query.WebSymbolsQueryExecutor
import com.intellij.webSymbols.utils.WebSymbolsIsolatedMappingScope
import org.angular2.Angular2Framework
import org.angular2.entities.Angular2Component
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.web.PROP_HOST_BINDING

class HostBindingsScope(mappings: Map<WebSymbolQualifiedKind, WebSymbolQualifiedKind>, decorator: ES6Decorator)
  : WebSymbolsIsolatedMappingScope<ES6Decorator>(mappings, Angular2Framework.ID, decorator) {

  override fun createPointer(): Pointer<HostBindingsScope> {
    val decoratorPtr = location.createSmartPointer()
    val mappings = mappings
    return Pointer {
      decoratorPtr.dereference()?.let { HostBindingsScope(mappings, it) }
    }
  }

  override fun isExclusiveFor(qualifiedKind: WebSymbolQualifiedKind): Boolean =
    mappings.containsKey(qualifiedKind)

  override fun acceptSymbol(symbol: WebSymbol): Boolean =
    (symbol.properties[PROP_HOST_BINDING] != false && (!symbol.name.startsWith("on") || !symbol.hasOnlyStandardHtmlSymbolsOrExtensions()))

  override val subScopeBuilder: (WebSymbolsQueryExecutor, ES6Decorator) -> List<WebSymbolsScope>
    get() = ::buildSubScope

  companion object {
    internal fun buildSubScope(executor: WebSymbolsQueryExecutor, location: ES6Decorator): List<WebSymbolsScope> {
      val file = location.containingFile
      val directive = Angular2EntitiesProvider.getDirective(location)
      val relatedStylesheets = (directive as? Angular2Component)?.cssFiles?.filterIsInstance<StylesheetFile>()
                               ?: emptyList()
      val scope = mutableSetOf<WebSymbolsScope>(
        StandardPropertyAndEventsScope(file),
        DirectiveElementSelectorsScope(file),
        DirectiveAttributeSelectorsScope(file),
        getStandardHtmlElementSymbolsScope(file.project),
        getWebSymbolsCssScopeForTagClasses(location, relatedStylesheets)
      )
      val elementNames = directive?.selector?.simpleSelectors
        ?.mapNotNull { it.elementName?.trim()?.takeIf { it.isNotEmpty() && it != "*" } }

      if (!elementNames.isNullOrEmpty()) {
        elementNames.forEach {
          scope.add(WebSymbolsHtmlQueryHelper.getStandardHtmlAttributeSymbolsScopeForTag(file.project, it))
        }
        val scopeList = scope.toList()
        elementNames.flatMapTo(scope) { executor.runNameMatchQuery(WebSymbol.HTML_ELEMENTS.withName(it), additionalScope = scopeList) }
        elementNames.mapTo(scope) { MatchedDirectivesScope.createFor(location, it) }
      }
      else {
        scope.add(WebSymbolsHtmlQueryHelper.getStandardHtmlAttributeSymbolsScopeForTag(file.project, "div"))
        executor.runNameMatchQuery(WebSymbol.HTML_ELEMENTS.withName("div"), additionalScope = scope.toList())
          .forEach { scope.add(it) }
      }
      return scope.toList()
    }
  }

}