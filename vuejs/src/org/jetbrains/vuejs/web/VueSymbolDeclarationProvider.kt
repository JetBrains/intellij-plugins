// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web

import com.intellij.lang.javascript.psi.*
import com.intellij.openapi.util.TextRange
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.declarations.PolySymbolDeclaration
import com.intellij.polySymbols.declarations.PolySymbolDeclarationProvider
import com.intellij.polySymbols.js.JS_EVENTS
import com.intellij.polySymbols.query.PolySymbolsNameMatchQueryParams
import com.intellij.polySymbols.query.PolySymbolsQueryExecutorFactory
import com.intellij.polySymbols.query.PolySymbolsScope
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.util.asSafely
import com.intellij.util.containers.Stack
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.index.getFunctionNameFromVueIndex
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.source.DEFINE_EMITS_FUN
import org.jetbrains.vuejs.model.source.EMITS_PROP
import org.jetbrains.vuejs.model.source.VueCompositionApp

class VueSymbolDeclarationProvider : PolySymbolDeclarationProvider {

  override fun getDeclarations(element: PsiElement, offsetInElement: Int): Collection<PolySymbolDeclaration> {
    val literal = element as? JSLiteralExpression ?: return emptyList()
    val name = getTextIfLiteral(literal) ?: return emptyList()

    val symbol = when (val parent = literal.parent) {
      is JSArgumentList -> {
        // "createApp()" syntax support
        val callExpr = parent.parent as? JSCallExpression ?: return emptyList()
        VueCompositionApp.getVueElement(callExpr)
          ?.asPolySymbol(name, VueModelVisitor.Proximity.APP)
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
              ?.takeIf { getFunctionNameFromVueIndex(it) == DEFINE_EMITS_FUN }
          else -> null
        }
          ?.let { VueModelManager.findEnclosingComponent(it) }
          ?.asPolySymbol("", VueModelVisitor.Proximity.LOCAL)
          ?.asSafely<PolySymbolsScope>()
          ?.getMatchingSymbols(JS_EVENTS.withName(name),
                               PolySymbolsNameMatchQueryParams.create(PolySymbolsQueryExecutorFactory.create(parent, false)),
                               Stack())
          ?.getOrNull(0)
          ?.asSafely<PolySymbol>()
      }
      else -> null
    }

    return symbol
             ?.let { listOf(VueSymbolDeclaration(it, literal)) }
           ?: emptyList()
  }

  private class VueSymbolDeclaration(
    private val symbol: PolySymbol,
    private val literal: JSLiteralExpression,
  ) : PolySymbolDeclaration {

    override fun getDeclaringElement(): PsiElement =
      literal

    override fun getRangeInDeclaringElement(): TextRange =
      ElementManipulators.getValueTextRange(literal)

    override fun getSymbol(): PolySymbol =
      symbol
  }
}