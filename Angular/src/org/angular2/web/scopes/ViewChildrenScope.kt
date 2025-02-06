package org.angular2.web.scopes

import com.intellij.javascript.webSymbols.decorateWithJsKindIcon
import com.intellij.javascript.webSymbols.decorateWithSymbolType
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.model.Pointer
import com.intellij.psi.PsiFile
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import com.intellij.webSymbols.WebSymbol.Companion.JS_STRING_LITERALS
import com.intellij.webSymbols.WebSymbol.Companion.JS_SYMBOLS
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.query.WebSymbolsCodeCompletionQueryParams
import com.intellij.webSymbols.query.WebSymbolsQueryExecutor
import com.intellij.webSymbols.utils.WebSymbolsIsolatedMappingScope
import org.angular2.Angular2Framework
import org.angular2.entities.Angular2EntitiesProvider

class ViewChildrenScope(
  decorator: ES6Decorator,
  private val resolveToMultipleSymbols: Boolean,
) : WebSymbolsIsolatedMappingScope<ES6Decorator>(
  mapOf(JS_STRING_LITERALS to JS_SYMBOLS), Angular2Framework.ID, decorator
) {

  override fun isExclusiveFor(qualifiedKind: WebSymbolQualifiedKind): Boolean =
    qualifiedKind == JS_STRING_LITERALS

  override fun acceptSymbol(symbol: WebSymbol): Boolean =
    true

  override val subScopeBuilder: (WebSymbolsQueryExecutor, ES6Decorator) -> List<WebSymbolsScope>
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

  override fun createPointer(): Pointer<out WebSymbolsScope> {
    val locationPtr = location.createSmartPointer()
    val resolveToMultipleSymbols = this.resolveToMultipleSymbols
    return Pointer {
      locationPtr.dereference()
        ?.let { ViewChildrenScope(it, resolveToMultipleSymbols) }
    }
  }

  private class ReferenceVariablesFlattenedScope(file: PsiFile, private val resolveToMultipleSymbols: Boolean)
    : WebSymbolsScopeWithCache<PsiFile, Boolean>(null, file.project, file, resolveToMultipleSymbols) {

    override fun getCodeCompletions(
      qualifiedName: WebSymbolQualifiedName,
      params: WebSymbolsCodeCompletionQueryParams,
      scope: Stack<WebSymbolsScope>,
    ): List<WebSymbolCodeCompletionItem> =
      super.getCodeCompletions(qualifiedName, params, scope)
        .map { it.decorateWithSymbolType(dataHolder, it.symbol).decorateWithJsKindIcon() }

    override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
      cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
      ReferenceVariablesStructuredScope(dataHolder).flattenSymbols(resolveToMultipleSymbols)
        .forEach(consumer)
    }

    override fun provides(qualifiedKind: WebSymbolQualifiedKind): Boolean =
      qualifiedKind == JS_SYMBOLS

    override fun createPointer(): Pointer<ReferenceVariablesFlattenedScope> {
      val filePtr = dataHolder.createSmartPointer()
      val resolveToMultipleSymbols = this.resolveToMultipleSymbols
      return Pointer {
        filePtr.dereference()?.let { ReferenceVariablesFlattenedScope(it, resolveToMultipleSymbols) }
      }
    }

  }

}