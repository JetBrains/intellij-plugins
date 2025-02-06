// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web.declarations

import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.psi.PsiElement
import com.intellij.webSymbols.declarations.WebSymbolDeclaration
import com.intellij.webSymbols.declarations.WebSymbolDeclarationProvider
import com.intellij.webSymbols.utils.WebSymbolDeclaredInPsi
import org.angular2.Angular2DecoratorUtil.INPUTS_PROP
import org.angular2.Angular2DecoratorUtil.MODEL_FUN
import org.angular2.Angular2DecoratorUtil.OUTPUTS_PROP
import org.angular2.entities.Angular2EntityUtils.getPropertyDeclarationOrReferenceKindAndDirective

class Angular2DirectivePropertyDeclarationProvider : WebSymbolDeclarationProvider {

  override fun getDeclarations(element: PsiElement, offsetInElement: Int): Collection<WebSymbolDeclaration> {
    if (element !is JSLiteralExpression || !element.isStringLiteral)
      return emptyList()

    val name = element.stringValue?.takeLastWhile { it != ':' }?.trim()?.takeIf { it.isNotEmpty() }
               ?: return emptyList()

    val (kind, directive) = getPropertyDeclarationOrReferenceKindAndDirective(element, true)
                            ?: return emptyList()

    return JSTypeEvaluationLocationProvider.withTypeEvaluationLocation(element) {
      when (kind) {
        INPUTS_PROP -> directive.inputs
        OUTPUTS_PROP -> directive.outputs
        MODEL_FUN -> directive.inOuts
        else -> emptyList()
      }
        .asSequence()
        .mapNotNull { property -> (property as? WebSymbolDeclaredInPsi)?.takeIf { it.name == name }?.declaration }
        .filter { it.declaringElement == element && (offsetInElement == -1 || it.rangeInDeclaringElement.contains(offsetInElement)) }
        .toList()
    }
  }
}
