package org.angular2.web.scopes

import com.intellij.html.polySymbols.PolySymbolsHtmlQueryHelper
import com.intellij.html.polySymbols.PolySymbolsHtmlQueryHelper.getStandardHtmlElementSymbolsScope
import com.intellij.html.polySymbols.hasOnlyStandardHtmlSymbolsOrExtensions
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.model.Pointer
import com.intellij.polySymbols.html.HTML_ELEMENTS
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolModifier
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.PolySymbolsScope
import com.intellij.polySymbols.css.getPolySymbolsCssScopeForTagClasses
import com.intellij.polySymbols.query.PolySymbolsQueryExecutor
import com.intellij.polySymbols.utils.PolySymbolsIsolatedMappingScope
import com.intellij.psi.createSmartPointer
import com.intellij.psi.css.StylesheetFile
import org.angular2.Angular2Framework
import org.angular2.entities.Angular2Component
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.web.PROP_HOST_BINDING

class HostBindingsScope(mappings: Map<PolySymbolQualifiedKind, PolySymbolQualifiedKind>, decorator: ES6Decorator)
  : PolySymbolsIsolatedMappingScope<ES6Decorator>(mappings, Angular2Framework.ID, decorator) {

  override fun createPointer(): Pointer<HostBindingsScope> {
    val decoratorPtr = location.createSmartPointer()
    val mappings = mappings
    return Pointer {
      decoratorPtr.dereference()?.let { HostBindingsScope(mappings, it) }
    }
  }

  override fun isExclusiveFor(qualifiedKind: PolySymbolQualifiedKind): Boolean =
    mappings.containsKey(qualifiedKind)

  override fun acceptSymbol(symbol: PolySymbol): Boolean =
    (symbol.properties[PROP_HOST_BINDING] != false && (!symbol.name.startsWith("on") || !symbol.hasOnlyStandardHtmlSymbolsOrExtensions()))

  override val subScopeBuilder: (PolySymbolsQueryExecutor, ES6Decorator) -> List<PolySymbolsScope>
    get() = ::buildSubScope

  companion object {
    internal fun buildSubScope(executor: PolySymbolsQueryExecutor, location: ES6Decorator): List<PolySymbolsScope> {
      val file = location.containingFile
      val directive = Angular2EntitiesProvider.getDirective(location)
      val relatedStylesheets = (directive as? Angular2Component)?.cssFiles?.filterIsInstance<StylesheetFile>()
                               ?: emptyList()
      val scope = mutableSetOf<PolySymbolsScope>(
        StandardPropertyAndEventsScope(file),
        DirectiveElementSelectorsScope(file),
        DirectiveAttributeSelectorsScope(file),
        getStandardHtmlElementSymbolsScope(file.project),
        getPolySymbolsCssScopeForTagClasses(location, relatedStylesheets),
      )
      val elementNames = directive?.selector?.simpleSelectors
        ?.mapNotNull { it.elementName?.trim()?.takeIf { it.isNotEmpty() && it != "*" } }

      if (!elementNames.isNullOrEmpty()) {
        elementNames.forEach {
          scope.add(PolySymbolsHtmlQueryHelper.getStandardHtmlAttributeSymbolsScopeForTag(file.project, it))
        }
        val scopeList = scope.toList()
        elementNames.flatMapTo(scope) {
          executor.nameMatchQuery(HTML_ELEMENTS, it)
            .exclude(PolySymbolModifier.ABSTRACT)
            .additionalScope(scopeList)
            .run()
        }
        elementNames.mapTo(scope) { MatchedDirectivesScope.createFor(location, it) }
      }
      else {
        scope.add(PolySymbolsHtmlQueryHelper.getStandardHtmlAttributeSymbolsScopeForTag(file.project, "div"))
        executor.nameMatchQuery(HTML_ELEMENTS, "div")
          .exclude(PolySymbolModifier.ABSTRACT)
          .additionalScope(scope.toList())
          .run()
          .forEach { scope.add(it) }
      }
      return scope.toList()
    }
  }

}