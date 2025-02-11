// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web.references

import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.model.psi.PsiExternalReferenceHost
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.asSafely
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.references.PsiWebSymbolReferenceProvider
import org.angular2.Angular2DecoratorUtil.COMPONENT_DEC
import org.angular2.Angular2DecoratorUtil.DIRECTIVE_DEC
import org.angular2.Angular2DecoratorUtil.SELECTOR_PROP
import org.angular2.Angular2DecoratorUtil.isLiteralInNgDecorator
import org.angular2.entities.Angular2DirectiveSelector
import org.angular2.entities.Angular2DirectiveSelectorSymbol
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.lang.html.psi.Angular2HtmlNgContentSelector

abstract class Angular2SelectorReferencesProvider : PsiWebSymbolReferenceProvider<PsiExternalReferenceHost> {

  class NgContentSelectorProvider : Angular2SelectorReferencesProvider() {

    override fun getDirectiveSelector(element: PsiExternalReferenceHost): Angular2DirectiveSelector? =
      element.asSafely<Angular2HtmlNgContentSelector>()?.selector
  }

  class NgDecoratorSelectorProvider : Angular2SelectorReferencesProvider() {

    override fun getDirectiveSelector(element: PsiExternalReferenceHost): Angular2DirectiveSelector? =
      if (isLiteralInNgDecorator(element, SELECTOR_PROP, COMPONENT_DEC, DIRECTIVE_DEC))
        Angular2EntitiesProvider.getDirective(PsiTreeUtil.getParentOfType(element, ES6Decorator::class.java))?.selector
      else null
  }

  override fun getOffsetsToReferencedSymbols(psiElement: PsiExternalReferenceHost): Map<Int, WebSymbol> {
    val directiveSelector = getDirectiveSelector(psiElement) ?: return emptyMap()
    return JSTypeEvaluationLocationProvider.withTypeEvaluationLocation(psiElement) {
      getReferencedSymbolsInner(directiveSelector)
    }
  }

  private fun getReferencedSymbolsInner(
    directiveSelector: Angular2DirectiveSelector,
  ): Map<Int, WebSymbol> {
    val result = mutableMapOf<Int, WebSymbol>()
    val add = { selector: Angular2DirectiveSelectorSymbol ->
      selector.referencedSymbol?.let {
        result[selector.textRangeInSourceElement.startOffset] = it
      }
    }
    for (selector in directiveSelector.simpleSelectorsWithPsi) {
      selector.element?.let { add(it) }
      for (attr in selector.attributes) {
        add(attr)
      }
      for (notSelector in selector.notSelectors) {
        for (attr in notSelector.attributes) {
          add(attr)
        }
      }
    }
    return result
  }

  protected abstract fun getDirectiveSelector(element: PsiExternalReferenceHost): Angular2DirectiveSelector?

}
