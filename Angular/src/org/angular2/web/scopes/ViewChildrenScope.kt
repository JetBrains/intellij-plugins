package org.angular2.web.scopes

import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolQualifiedName
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.js.JS_STRING_LITERALS
import com.intellij.polySymbols.js.JS_SYMBOLS
import com.intellij.polySymbols.js.decorateWithJsKindIcon
import com.intellij.polySymbols.js.decorateWithSymbolType
import com.intellij.polySymbols.query.PolySymbolCodeCompletionQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryExecutor
import com.intellij.polySymbols.query.PolySymbolQueryStack
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.polySymbols.utils.PolySymbolIsolatedMappingScope
import com.intellij.polySymbols.utils.PolySymbolScopeWithCache
import com.intellij.psi.PsiFile
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import org.angular2.Angular2Framework
import org.angular2.entities.Angular2EntitiesProvider

class ViewChildrenScope(
  decorator: ES6Decorator,
  private val resolveToMultipleSymbols: Boolean,
) : PolySymbolIsolatedMappingScope<ES6Decorator>(
  mapOf(JS_STRING_LITERALS to JS_SYMBOLS), Angular2Framework.ID, decorator
) {

  override fun isExclusiveFor(kind: PolySymbolKind): Boolean =
    kind == JS_STRING_LITERALS

  override fun acceptSymbol(symbol: PolySymbol): Boolean =
    true

  override val subScopeBuilder: (PolySymbolQueryExecutor, ES6Decorator) -> List<PolySymbolScope>
    get() = if (resolveToMultipleSymbols) { executor, decorator ->
      listOfNotNull(
        Angular2EntitiesProvider.getComponent(decorator)
          ?.templateFile
          ?.let { ReferenceVariablesFlattenedScope(it, true) }
      )
    }
    else { executor, decorator ->
      listOfNotNull(
        Angular2EntitiesProvider.getComponent(decorator)
          ?.templateFile
          ?.let { ReferenceVariablesFlattenedScope(it, false) }
      )
    }

  override fun createPointer(): Pointer<out PolySymbolScope> {
    val locationPtr = location.createSmartPointer()
    val resolveToMultipleSymbols = this.resolveToMultipleSymbols
    return Pointer {
      locationPtr.dereference()
        ?.let { ViewChildrenScope(it, resolveToMultipleSymbols) }
    }
  }

  private class ReferenceVariablesFlattenedScope(file: PsiFile, private val resolveToMultipleSymbols: Boolean) :
    PolySymbolScopeWithCache<PsiFile, Boolean>(null, file.project, file, resolveToMultipleSymbols) {

    override fun getCodeCompletions(
      qualifiedName: PolySymbolQualifiedName,
      params: PolySymbolCodeCompletionQueryParams,
      stack: PolySymbolQueryStack,
    ): List<PolySymbolCodeCompletionItem> =
      super.getCodeCompletions(qualifiedName, params, stack)
        .map { it.decorateWithSymbolType(dataHolder, it.symbol).decorateWithJsKindIcon() }

    override fun initialize(consumer: (PolySymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
      cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
      ReferenceVariablesStructuredScope(dataHolder).flattenSymbols(resolveToMultipleSymbols)
        .forEach(consumer)
    }

    override fun provides(kind: PolySymbolKind): Boolean =
      kind == JS_SYMBOLS

    override fun createPointer(): Pointer<ReferenceVariablesFlattenedScope> {
      val filePtr = dataHolder.createSmartPointer()
      val resolveToMultipleSymbols = this.resolveToMultipleSymbols
      return Pointer {
        filePtr.dereference()?.let { ReferenceVariablesFlattenedScope(it, resolveToMultipleSymbols) }
      }
    }

  }

}