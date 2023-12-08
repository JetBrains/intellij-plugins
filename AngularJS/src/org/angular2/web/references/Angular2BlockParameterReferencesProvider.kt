// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web.references

import com.intellij.psi.util.childLeafs
import com.intellij.psi.util.elementType
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.references.WebSymbolReferenceProvider
import org.angular2.lang.expr.lexer.Angular2TokenTypes
import org.angular2.lang.expr.psi.Angular2BlockParameter

class Angular2BlockParameterReferencesProvider : WebSymbolReferenceProvider<Angular2BlockParameter>() {

  override fun getOffsetsToSymbols(psiElement: Angular2BlockParameter): Map<Int, WebSymbol> {
    if (psiElement.isPrimaryExpression) {
      return emptyMap()
    }
    else {
      val definitions = psiElement.block?.definition?.parameters ?: return emptyMap()
      return psiElement.childLeafs
        .filter { it.elementType == Angular2TokenTypes.BLOCK_PARAMETER_NAME }
        .mapNotNull {
          val name = it.text
          definitions.find { def -> def.name == name }
            ?.let { symbol -> it.startOffsetInParent to symbol }
        }
        .toMap()
    }
  }
}