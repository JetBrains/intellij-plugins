package org.angular2.web.findUsages

import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.polySymbols.search.PsiSourcedPolySymbolProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.contextOfType
import org.angular2.entities.Angular2EntitiesProvider

class Angular2PsiSourcedPolySymbolProvider : PsiSourcedPolySymbolProvider {

  override fun getSymbols(element: PsiElement): List<PsiSourcedPolySymbol> =
    if (element is TypeScriptField) {
      JSTypeEvaluationLocationProvider.withTypeEvaluationLocation(element) {
        Angular2EntitiesProvider.getDirective(element.contextOfType<TypeScriptClass>())
          ?.let { it.inOuts + it.inputs }
          ?.find { (it as? PsiSourcedPolySymbol)?.source == element }
          ?.let {
            listOf(it as PsiSourcedPolySymbol)
          }
      } ?: emptyList()
    }
    else
      emptyList()

}