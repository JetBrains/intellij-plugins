// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web

import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolDeclaration
import com.intellij.webSymbols.WebSymbolDeclarationProvider
import com.intellij.lang.javascript.psi.JSArgumentList
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.openapi.util.TextRange
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.source.VueCompositionApp

class VueSymbolDeclarationProvider : WebSymbolDeclarationProvider {

  override fun getDeclarations(element: PsiElement, offsetInElement: Int): Collection<WebSymbolDeclaration> {
    val literal = element as? JSLiteralExpression ?: return emptyList()

    // "createApp()" syntax support
    val callExpr = (literal.parent as? JSArgumentList)?.parent as? JSCallExpression ?: return emptyList()
    val name = getTextIfLiteral(literal) ?: return emptyList()

    return VueCompositionApp.getVueElement(callExpr)
             ?.asWebSymbol(name, VueModelVisitor.Proximity.APP)
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