// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web.declarations

import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.polySymbols.declarations.PolySymbolDeclaration
import com.intellij.polySymbols.declarations.PolySymbolDeclarationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.angular2.Angular2DecoratorUtil
import org.angular2.entities.Angular2EntitiesProvider

class Angular2DirectiveExportAsDeclarationProvider : PolySymbolDeclarationProvider {
  override fun getDeclarations(element: PsiElement, offsetInElement: Int): Collection<PolySymbolDeclaration> =
    if (Angular2DecoratorUtil.isLiteralInNgDecorator(element, Angular2DecoratorUtil.EXPORT_AS_PROP,
                                                     Angular2DecoratorUtil.COMPONENT_DEC, Angular2DecoratorUtil.DIRECTIVE_DEC))
      JSTypeEvaluationLocationProvider.withTypeEvaluationLocation(element) {
        Angular2EntitiesProvider.getDirective(PsiTreeUtil.getParentOfType(element, ES6Decorator::class.java))
          ?.exportAs
          ?.values
          ?.let { exports ->
            if (offsetInElement < 0)
              exports.mapNotNull { it.declaration }
            else
              exports.find { it.sourceElement == element && it.textRangeInSourceElement?.contains(offsetInElement) == true }
                ?.declaration
                ?.let { listOf(it) }
          }
        ?: emptyList()
      }
    else
      emptyList()

}