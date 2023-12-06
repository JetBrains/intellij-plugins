// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web.scopes

import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolsScopeWithCache
import org.angular2.Angular2Framework
import org.angular2.codeInsight.template.Angular2TemplateScope
import org.angular2.codeInsight.template.Angular2TemplateScopesResolver

class WebSymbolsTemplateScope(context: PsiElement) :
  WebSymbolsScopeWithCache<PsiElement, Unit>(Angular2Framework.ID, context.project, context, Unit) {
  override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
    val providedSymbols = mutableSetOf<String>()
    val scopeSymbols = mutableSetOf<String>()
    for (scope in Angular2TemplateScopesResolver.getScopes(dataHolder)) {
      var currentScope: Angular2TemplateScope? = scope
      while (currentScope != null) {
        scopeSymbols.clear()
        for (symbol in currentScope.symbols) {
          val name = symbol.name
          if (symbol.pattern == null && !providedSymbols.contains(name)) {
            consumer(symbol)
            scopeSymbols.add(name)
          }
        }
        providedSymbols.addAll(scopeSymbols)
        currentScope = currentScope.parent
      }
    }
  }

  override fun createPointer(): Pointer<out WebSymbolsScopeWithCache<PsiElement, Unit>> {
    val context = dataHolder.createSmartPointer()
    return Pointer {
      context.dereference()?.let { WebSymbolsTemplateScope(it) }
    }
  }

}