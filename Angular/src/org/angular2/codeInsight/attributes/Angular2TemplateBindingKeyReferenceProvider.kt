package org.angular2.codeInsight.attributes

import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbol.Companion.JS_PROPERTIES
import com.intellij.webSymbols.query.WebSymbolsQueryExecutorFactory
import com.intellij.webSymbols.references.PsiWebSymbolReferenceProvider
import com.intellij.webSymbols.utils.asSingleSymbol
import org.angular2.lang.expr.psi.Angular2TemplateBinding
import org.angular2.lang.expr.psi.Angular2TemplateBindingKey
import org.angular2.web.NG_TEMPLATE_BINDINGS

class Angular2TemplateBindingKeyReferenceProvider : PsiWebSymbolReferenceProvider<Angular2TemplateBindingKey> {

  override fun getReferencedSymbol(psiElement: Angular2TemplateBindingKey): WebSymbol? =
    when ((psiElement.parent as? Angular2TemplateBinding)?.keyKind) {
      Angular2TemplateBinding.KeyKind.LET -> WebSymbolsQueryExecutorFactory.create(psiElement)
        .runNameMatchQuery(JS_PROPERTIES.withName(psiElement.name))
        .asSingleSymbol()

      Angular2TemplateBinding.KeyKind.BINDING ->
        WebSymbolsQueryExecutorFactory.create(psiElement)
          .runNameMatchQuery(NG_TEMPLATE_BINDINGS.withName(psiElement.name))
          .asSingleSymbol()

      else -> null
    }

  override fun getReferencedSymbolNameOffset(psiElement: Angular2TemplateBindingKey): Int = 0

}