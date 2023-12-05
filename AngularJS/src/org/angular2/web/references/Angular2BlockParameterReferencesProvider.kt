// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web.references

import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.references.WebSymbolReferenceProvider
import org.angular2.lang.expr.psi.Angular2BlockParameter

class Angular2BlockParameterReferencesProvider : WebSymbolReferenceProvider<Angular2BlockParameter>() {

  override fun getSymbol(psiElement: Angular2BlockParameter): WebSymbol? =
    psiElement.definition?.takeIf { !it.isPrimaryExpression }

  override fun getSymbolNameOffset(psiElement: Angular2BlockParameter): Int =
    psiElement.nameElement?.startOffsetInParent ?: 0

}