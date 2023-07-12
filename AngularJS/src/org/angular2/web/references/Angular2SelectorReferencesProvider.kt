// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web.references

import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.model.Symbol
import com.intellij.model.psi.PsiExternalReferenceHost
import com.intellij.model.psi.PsiSymbolReference
import com.intellij.model.psi.PsiSymbolReferenceHints
import com.intellij.model.psi.PsiSymbolReferenceProvider
import com.intellij.model.search.SearchRequest
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.SmartList
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.references.WebSymbolReference
import org.angular2.Angular2DecoratorUtil.COMPONENT_DEC
import org.angular2.Angular2DecoratorUtil.DIRECTIVE_DEC
import org.angular2.Angular2DecoratorUtil.SELECTOR_PROP
import org.angular2.Angular2DecoratorUtil.isLiteralInNgDecorator
import org.angular2.entities.Angular2DirectiveSelector
import org.angular2.entities.Angular2DirectiveSelectorSymbol
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.lang.html.psi.Angular2HtmlNgContentSelector
import org.angular2.web.Angular2Symbol

abstract class Angular2SelectorReferencesProvider : PsiSymbolReferenceProvider {

  class NgContentSelectorProvider : Angular2SelectorReferencesProvider() {

    override fun getDirectiveSelector(element: PsiExternalReferenceHost): Angular2DirectiveSelector? {
      return if (element is Angular2HtmlNgContentSelector) {
        element.selector
      }
      else null
    }
  }

  class NgDecoratorSelectorProvider : Angular2SelectorReferencesProvider() {

    override fun getDirectiveSelector(element: PsiExternalReferenceHost): Angular2DirectiveSelector? {
      return if (isLiteralInNgDecorator(element, SELECTOR_PROP, COMPONENT_DEC, DIRECTIVE_DEC)) {
        Angular2EntitiesProvider.getDirective(PsiTreeUtil.getParentOfType(element, ES6Decorator::class.java))?.selector
      }
      else null
    }
  }

  override fun getSearchRequests(project: Project,
                                 target: Symbol): Collection<SearchRequest> {
    return emptyList()
  }

  override fun getReferences(element: PsiExternalReferenceHost,
                             hints: PsiSymbolReferenceHints): Collection<PsiSymbolReference> {
    val directiveSelector = getDirectiveSelector(element) ?: return emptyList()
    val result = SmartList<PsiSymbolReference>()
    val add = { selector: Angular2DirectiveSelectorSymbol ->
      if (!selector.isDeclaration) {
        result.add(HtmlSelectorReference(selector))
      }
    }
    val offsetInTheElement = hints.offsetInElement
    if (offsetInTheElement >= 0) {
      for (selector in directiveSelector.simpleSelectorsWithPsi) {
        val found = selector.getElementAt(offsetInTheElement)
        if (found != null) {
          add(found)
          return result
        }
      }
      return emptyList()
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

  private class HtmlSelectorReference(private val mySelectorSymbol: Angular2DirectiveSelectorSymbol) : WebSymbolReference {

    override fun getElement(): PsiElement {
      return mySelectorSymbol.sourceElement
    }

    override fun getRangeInElement(): TextRange {
      return mySelectorSymbol.textRangeInSourceElement
    }

    override fun resolveReference(): Collection<WebSymbol> {
      val symbols = mySelectorSymbol.referencedSymbols
      val nonSelectorSymbols = symbols.filter { it !is Angular2Symbol }
      return if (!nonSelectorSymbols.isEmpty()) {
        nonSelectorSymbols
      }
      else {
        symbols
      }
    }
  }
}
