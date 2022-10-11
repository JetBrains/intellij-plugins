// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web

import com.intellij.lang.javascript.psi.*
import com.intellij.openapi.util.TextRange
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.util.asSafely
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbol.Companion.KIND_JS_EVENTS
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_JS
import com.intellij.webSymbols.declarations.WebSymbolDeclaration
import com.intellij.webSymbols.declarations.WebSymbolDeclarationProvider
import com.intellij.webSymbols.registry.WebSymbolsNameMatchQueryParams
import com.intellij.webSymbols.registry.WebSymbolsRegistryManager
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.index.VueFrameworkHandler
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.source.DEFINE_EMITS_FUN
import org.jetbrains.vuejs.model.source.EMITS_PROP
import org.jetbrains.vuejs.model.source.VueCompositionApp

class VueSymbolDeclarationProvider : WebSymbolDeclarationProvider {

  override fun getDeclarations(element: PsiElement, offsetInElement: Int): Collection<WebSymbolDeclaration> {
    val literal = element as? JSLiteralExpression ?: return emptyList()
    val name = getTextIfLiteral(literal) ?: return emptyList()

    val symbol = when (val parent = literal.parent) {
      is JSArgumentList -> {
        // "createApp()" syntax support
        val callExpr = parent.parent as? JSCallExpression ?: return emptyList()
        VueCompositionApp.getVueElement(callExpr)
          ?.asWebSymbol(name, VueModelVisitor.Proximity.APP)
      }
      is JSArrayLiteralExpression -> {
        when (val grandparent = parent.parent) {
          // "emits" property
          is JSProperty ->
            grandparent.takeIf { it.name == EMITS_PROP }

          // "defineEmits" call
          is JSArgumentList ->
            grandparent.parent
              ?.asSafely<JSCallExpression>()
              ?.takeIf { VueFrameworkHandler.getFunctionNameFromVueIndex(it) == DEFINE_EMITS_FUN }
          else -> null
        }
          ?.let { VueModelManager.findEnclosingComponent(it) }
          ?.asWebSymbol("", VueModelVisitor.Proximity.LOCAL)
          ?.getSymbols(NAMESPACE_JS, KIND_JS_EVENTS, name,
                       WebSymbolsNameMatchQueryParams(WebSymbolsRegistryManager.get(parent, false)),
                       Stack())
          ?.getOrNull(0)
          ?.asSafely<WebSymbol>()
      }
      else -> null
    }

    return symbol
             ?.let { listOf(VueSymbolDeclaration(it, literal)) }
           ?: emptyList()
  }

  private class VueSymbolDeclaration constructor(private val symbol: WebSymbol,
                                                 private val literal: JSLiteralExpression) : WebSymbolDeclaration {

    override fun getDeclaringElement(): PsiElement =
      literal

    override fun getRangeInDeclaringElement(): TextRange =
      ElementManipulators.getValueTextRange(literal)

    override fun getSymbol(): WebSymbol =
      symbol
  }
}