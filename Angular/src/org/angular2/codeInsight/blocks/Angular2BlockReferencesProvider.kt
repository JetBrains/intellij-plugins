// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.blocks

import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.references.PsiWebSymbolReferenceProvider
import org.angular2.lang.html.psi.Angular2HtmlBlock

class Angular2BlockReferencesProvider : PsiWebSymbolReferenceProvider<Angular2HtmlBlock> {

  override fun getReferencedSymbol(psiElement: Angular2HtmlBlock): WebSymbol? =
    psiElement.definition

  override fun getReferencedSymbolNameOffset(psiElement: Angular2HtmlBlock): Int = 1

}