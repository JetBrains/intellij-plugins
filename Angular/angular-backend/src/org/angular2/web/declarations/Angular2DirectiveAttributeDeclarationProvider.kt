package org.angular2.web.declarations

import com.intellij.javascript.JSBuiltInTypeEngineEvaluation
import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider
import com.intellij.lang.javascript.psi.JSArgumentList
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.declarations.PolySymbolDeclaration
import com.intellij.polySymbols.declarations.PolySymbolDeclarationProvider
import com.intellij.polySymbols.utils.PolySymbolDeclaredInPsi
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.asSafely
import org.angular2.entities.Angular2EntitiesProvider

class Angular2DirectiveAttributeDeclarationProvider : PolySymbolDeclarationProvider {

  override fun getDeclarations(element: PsiElement, offsetInElement: Int): Collection<PolySymbolDeclaration> {
    if (element !is JSLiteralExpression || !element.isStringLiteral || element.parent !is JSArgumentList)
      return emptyList()

    val name = element.stringValue?.takeIf { it.isNotBlank() }
               ?: return emptyList()

    return JSTypeEvaluationLocationProvider.withTypeEvaluationLocation(element) {
      Angular2EntitiesProvider.getDirective(PsiTreeUtil.getParentOfType(element, TypeScriptClass::class.java))
        ?.attributes
        ?.find { it.name == name && it.sourceElement == element }
        ?.asSafely<PolySymbolDeclaredInPsi>()
        ?.declaration
        ?.let { listOf(it) }
      ?: emptyList()
    }
  }

  override fun getEquivalentDeclarations(element: PsiElement, offsetInElement: Int, target: PolySymbol): Collection<PolySymbolDeclaration> {
    return JSBuiltInTypeEngineEvaluation.forceBuiltInTypeEngineIfNeeded(element, target.psiContext) {
      super.getEquivalentDeclarations(element, offsetInElement, target)
    }
  }
}