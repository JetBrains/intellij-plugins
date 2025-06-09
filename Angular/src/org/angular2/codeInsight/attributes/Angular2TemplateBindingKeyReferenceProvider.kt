package org.angular2.codeInsight.attributes

import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.js.JS_PROPERTIES
import com.intellij.polySymbols.query.PolySymbolsQueryExecutorFactory
import com.intellij.polySymbols.references.PsiPolySymbolReferenceProvider
import com.intellij.polySymbols.utils.asSingleSymbol
import org.angular2.lang.expr.psi.Angular2TemplateBinding
import org.angular2.lang.expr.psi.Angular2TemplateBindingKey
import org.angular2.web.NG_TEMPLATE_BINDINGS

class Angular2TemplateBindingKeyReferenceProvider : PsiPolySymbolReferenceProvider<Angular2TemplateBindingKey> {

  override fun getReferencedSymbol(psiElement: Angular2TemplateBindingKey): PolySymbol? =
    when ((psiElement.parent as? Angular2TemplateBinding)?.keyKind) {
      Angular2TemplateBinding.KeyKind.LET -> PolySymbolsQueryExecutorFactory.create(psiElement)
        .runNameMatchQuery(JS_PROPERTIES, psiElement.name)
        .asSingleSymbol()

      Angular2TemplateBinding.KeyKind.BINDING ->
        PolySymbolsQueryExecutorFactory.create(psiElement)
          .runNameMatchQuery(NG_TEMPLATE_BINDINGS, psiElement.name)
          .asSingleSymbol()

      else -> null
    }

  override fun getReferencedSymbolNameOffset(psiElement: Angular2TemplateBindingKey): Int = 0

}