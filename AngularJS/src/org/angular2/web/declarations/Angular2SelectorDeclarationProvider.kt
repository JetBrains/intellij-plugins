// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web.declarations

import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.webSymbols.declarations.WebSymbolDeclaration
import com.intellij.webSymbols.declarations.WebSymbolDeclarationProvider
import org.angular2.Angular2DecoratorUtil.COMPONENT_DEC
import org.angular2.Angular2DecoratorUtil.DIRECTIVE_DEC
import org.angular2.Angular2DecoratorUtil.SELECTOR_PROP
import org.angular2.Angular2DecoratorUtil.isLiteralInNgDecorator
import org.angular2.entities.Angular2DirectiveSelector
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.lang.html.psi.Angular2HtmlNgContentSelector

class Angular2SelectorDeclarationProvider : WebSymbolDeclarationProvider {

  override fun getDeclarations(element: PsiElement, offsetInElement: Int): Collection<WebSymbolDeclaration> {
    val directiveSelector: Angular2DirectiveSelector = when {
                                                         element is Angular2HtmlNgContentSelector ->
                                                           element.selector
                                                         isLiteralInNgDecorator(element, SELECTOR_PROP, COMPONENT_DEC, DIRECTIVE_DEC) ->
                                                           Angular2EntitiesProvider.getDirective(
                                                             PsiTreeUtil.getParentOfType(element, ES6Decorator::class.java))?.selector
                                                         else -> null
                                                       } ?: return emptyList()

    for (selector in directiveSelector.simpleSelectorsWithPsi) {
      val selectorPart = selector.getElementAt(offsetInElement)
      if (selectorPart != null) {
        if (selectorPart.isDeclaration) {
          selectorPart.declaration?.let { return setOf(it) }
        }
        break
      }
    }
    return emptyList()
  }
}
