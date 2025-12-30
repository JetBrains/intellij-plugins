package org.angular2.codeInsight.attributes

import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.js.JS_PROPERTIES
import com.intellij.polySymbols.query.PolySymbolQueryExecutorFactory
import com.intellij.polySymbols.references.PsiPolySymbolReferenceProvider
import com.intellij.polySymbols.utils.asSingleSymbol
import org.angular2.lang.expr.psi.Angular2TemplateBinding
import org.angular2.lang.expr.psi.Angular2TemplateBindingKey
import org.angular2.web.NG_TEMPLATE_BINDINGS

class Angular2TemplateBindingKeyReferenceProvider : PsiPolySymbolReferenceProvider<Angular2TemplateBindingKey> {

  override fun getReferencedSymbol(psiElement: Angular2TemplateBindingKey): PolySymbol? =
    when ((psiElement.parent as? Angular2TemplateBinding)?.keyKind) {
      Angular2TemplateBinding.KeyKind.LET -> PolySymbolQueryExecutorFactory.create(psiElement)
        .nameMatchQuery(JS_PROPERTIES, psiElement.name)
        .run()
        .asSingleSymbol()

      Angular2TemplateBinding.KeyKind.BINDING ->
        PolySymbolQueryExecutorFactory.create(psiElement)
          .nameMatchQuery(NG_TEMPLATE_BINDINGS, psiElement.name)
          .run()
          .asSingleSymbol()

      else -> null
    }

  override fun getReferencedSymbolNameOffset(psiElement: Angular2TemplateBindingKey): Int = 0

}