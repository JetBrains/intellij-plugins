package org.angular2.web.findUsages

import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.polySymbols.search.PsiLinkedPolySymbol
import com.intellij.polySymbols.search.PsiLinkedPolySymbolProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.contextOfType
import org.angular2.entities.Angular2EntitiesProvider

class Angular2PsiLinkedPolySymbolProvider : PsiLinkedPolySymbolProvider {

  override fun getSymbols(element: PsiElement): List<PsiLinkedPolySymbol> =
    if (element is TypeScriptField) {
      JSTypeEvaluationLocationProvider.withTypeEvaluationLocation(element) {
        Angular2EntitiesProvider.getDirective(element.contextOfType<TypeScriptClass>())
          ?.let { it.inOuts + it.inputs }
          ?.find { (it as? PsiLinkedPolySymbol)?.linkedElement == element }
          ?.let {
            listOf(it as PsiLinkedPolySymbol)
          }
      } ?: emptyList()
    }
    else
      emptyList()

}