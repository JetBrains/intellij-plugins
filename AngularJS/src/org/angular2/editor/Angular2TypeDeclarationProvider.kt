// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.editor

import com.intellij.codeInsight.navigation.SymbolTypeProvider
import com.intellij.codeInsight.navigation.actions.TypeDeclarationProvider
import com.intellij.model.Symbol
import com.intellij.model.psi.PsiSymbolService
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.psi.PsiElement
import com.intellij.util.asSafely
import com.intellij.webSymbols.WebSymbol
import org.angular2.entities.Angular2Component
import org.angular2.lang.Angular2Bundle
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.PROP_SYMBOL_DIRECTIVE

class Angular2TypeDeclarationProvider : TypeDeclarationProvider, SymbolTypeProvider {

  override fun getSymbolTypeDeclarations(symbol: PsiElement): Array<PsiElement>? {
    return null
  }

  override fun getActionText(context: DataContext): String? {
    val directives = Angular2EditorUtils.getDirectivesAtCaret(context)
    return if (directives.any { it.isComponent }) {
      Angular2Bundle.message("angular.action.goto-type-declaration.component-template")
    }
    else null
  }

  override fun getSymbolTypes(symbol: Symbol): List<Symbol> {
    if (symbol is WebSymbol) {
      symbol.properties[PROP_SYMBOL_DIRECTIVE]
        ?.asSafely<Angular2Component>()
        ?.templateFile
        ?.let {
          return listOf(PsiSymbolService.getInstance().asSymbol(it))
        }
    }
    return emptyList()
  }
}
