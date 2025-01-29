package org.angular2.codeInsight.attributes

import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.query.WebSymbolsQueryExecutorFactory
import com.intellij.webSymbols.references.WebSymbolReferenceProvider
import com.intellij.webSymbols.utils.asSingleSymbol
import org.angular2.lang.expr.psi.Angular2TemplateBindingKey
import org.angular2.web.NG_TEMPLATE_BINDINGS

class Angular2TemplateBindingKeyReferenceProvider : WebSymbolReferenceProvider<Angular2TemplateBindingKey>() {

  override fun getSymbol(psiElement: Angular2TemplateBindingKey): WebSymbol? =
    WebSymbolsQueryExecutorFactory.create(psiElement)
      .runNameMatchQuery(NG_TEMPLATE_BINDINGS.withName(psiElement.name))
      .asSingleSymbol()

  override fun getSymbolNameOffset(psiElement: Angular2TemplateBindingKey): Int = 0

}