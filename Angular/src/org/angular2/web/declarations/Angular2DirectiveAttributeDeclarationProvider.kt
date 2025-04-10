package org.angular2.web.declarations

import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider
import com.intellij.lang.javascript.psi.JSArgumentList
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.asSafely
import com.intellij.webSymbols.declarations.WebSymbolDeclaration
import com.intellij.webSymbols.declarations.WebSymbolDeclarationProvider
import com.intellij.webSymbols.utils.WebSymbolDeclaredInPsi
import org.angular2.entities.Angular2EntitiesProvider

class Angular2DirectiveAttributeDeclarationProvider : WebSymbolDeclarationProvider {

  override fun getDeclarations(element: PsiElement, offsetInElement: Int): Collection<WebSymbolDeclaration> {
    if (element !is JSLiteralExpression || !element.isStringLiteral || element.parent !is JSArgumentList)
      return emptyList()

    val name = element.stringValue?.takeIf { it.isNotBlank() }
               ?: return emptyList()

    return JSTypeEvaluationLocationProvider.withTypeEvaluationLocation(element) {
      Angular2EntitiesProvider . getDirective (PsiTreeUtil.getParentOfType(element, TypeScriptClass::class.java))
        ?.attributes
        ?.find { it.name == name && it.sourceElement == element }
        ?.asSafely<WebSymbolDeclaredInPsi>()
        ?.declaration
        ?.let { listOf(it) }
      ?: emptyList()
    }
  }
}