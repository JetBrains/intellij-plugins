// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web.references

import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.references.WebSymbolReferenceProvider
import org.angular2.lang.html.psi.Angular2HtmlBlock

class Angular2BlockReferencesProvider : WebSymbolReferenceProvider<Angular2HtmlBlock>() {

  override fun getSymbol(psiElement: Angular2HtmlBlock): WebSymbol? =
    psiElement.definition

  override fun getSymbolNameOffset(psiElement: Angular2HtmlBlock): Int = 1

}