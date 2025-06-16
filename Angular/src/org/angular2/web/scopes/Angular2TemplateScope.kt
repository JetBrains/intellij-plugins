// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web.scopes

import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.js.JS_SYMBOLS
import com.intellij.polySymbols.query.PolySymbolWithPattern
import com.intellij.polySymbols.utils.PolySymbolScopeWithCache
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import org.angular2.Angular2Framework
import org.angular2.codeInsight.template.Angular2TemplateScope
import org.angular2.codeInsight.template.Angular2TemplateScopesResolver

class Angular2TemplateScope(context: PsiElement) :
  PolySymbolScopeWithCache<PsiElement, Unit>(Angular2Framework.ID, context.project, context, Unit) {

  override fun provides(qualifiedKind: PolySymbolQualifiedKind): Boolean =
    qualifiedKind == JS_SYMBOLS

  override fun initialize(consumer: (PolySymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
    val providedSymbols = mutableSetOf<String>()
    val scopeSymbols = mutableSetOf<String>()
    for (scope in Angular2TemplateScopesResolver.getScopes(dataHolder)) {
      var currentScope: Angular2TemplateScope? = scope
      while (currentScope != null) {
        scopeSymbols.clear()
        for (symbol in currentScope.symbols) {
          val name = symbol.name
          if (symbol !is PolySymbolWithPattern && !providedSymbols.contains(name)) {
            consumer(symbol)
            scopeSymbols.add(name)
          }
        }
        providedSymbols.addAll(scopeSymbols)
        currentScope = currentScope.parent
      }
    }
  }

  override fun createPointer(): Pointer<out PolySymbolScopeWithCache<PsiElement, Unit>> {
    val context = dataHolder.createSmartPointer()
    return Pointer {
      context.dereference()?.let { Angular2TemplateScope(it) }
    }
  }
}