// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.blocks

import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.references.PsiWebSymbolReferenceProvider
import org.angular2.lang.expr.psi.Angular2BlockParameter

class Angular2BlockParameterReferencesProvider : PsiWebSymbolReferenceProvider<Angular2BlockParameter> {

  override fun getOffsetsToReferencedSymbols(psiElement: Angular2BlockParameter): Map<Int, WebSymbol> {
    if (psiElement.isPrimaryExpression) {
      return emptyMap()
    }
    else {
      return listOf(
        psiElement.prefixElement to psiElement.prefixDefinition,
        psiElement.nameElement to psiElement.definition
      )
        .mapNotNull { (element, definition) ->
          if (definition != null && element != null)
            element.startOffsetInParent to definition
          else
            null
        }
        .toMap()
    }
  }
}