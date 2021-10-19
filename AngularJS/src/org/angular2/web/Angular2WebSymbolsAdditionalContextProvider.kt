// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web

import com.intellij.javascript.web.symbols.WebSymbolsAdditionalContextProvider
import com.intellij.javascript.web.symbols.WebSymbolsContainer
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlTag
import com.intellij.util.castSafelyTo
import org.angular2.Angular2Framework
import org.angular2.web.containers.*

class Angular2WebSymbolsAdditionalContextProvider : WebSymbolsAdditionalContextProvider {

  override fun getAdditionalContext(element: PsiElement?, framework: String?): List<WebSymbolsContainer> =
    element
      ?.castSafelyTo<XmlTag>()
      ?.takeIf { framework == Angular2Framework.ID }
      ?.let {
        listOf(
          OneTimeBindingsProvider(),
          DirectiveElementSelectorsContainer(it.project),
          DirectiveAttributeSelectorsContainer(it.project),
          StandardPropertyAndEventsContainer(it.containingFile),
          NgContentSelectorsContainer(it),
          MatchedDirectivesContainer(it),
          I18nAttributesContainer(it),
        )
      }
    ?: emptyList()

  companion object {
    const val PROP_BINDING_PATTERN = "ng-binding-pattern"
    const val PROP_ERROR_SYMBOL = "ng-error-symbol"
    const val PROP_SYMBOL_DIRECTIVE = "ng-symbol-directive"
    const val PROP_SCOPE_PROXIMITY = "scope-proximity"

    const val EVENT_ATTR_PREFIX = "on"

    const val ATTR_NG_NON_BINDABLE = "ngNonBindable"
    const val ATTR_SELECT = "select"

    const val ELEMENT_NG_CONTAINER = "ng-container"
    const val ELEMENT_NG_CONTENT = "ng-content"
    const val ELEMENT_NG_TEMPLATE = "ng-template"

    const val KIND_NG_STRUCTURAL_DIRECTIVES = "ng-structural-directives"

    const val KIND_NG_DIRECTIVE_ONE_TIME_BINDINGS = "ng-one-time-bindings"
    const val KIND_NG_DIRECTIVE_INPUTS = "ng-directive-inputs"
    const val KIND_NG_DIRECTIVE_OUTPUTS = "ng-directive-outputs"
    const val KIND_NG_DIRECTIVE_IN_OUTS = "ng-directive-in-outs"
    const val KIND_NG_DIRECTIVE_ATTRIBUTES = "ng-directive-attributes"

    const val KIND_NG_DIRECTIVE_ELEMENT_SELECTORS = "ng-directive-element-selectors"
    const val KIND_NG_DIRECTIVE_ATTRIBUTE_SELECTORS = "ng-directive-attribute-selectors"

    const val KIND_NG_I18N_ATTRIBUTES = "ng-i18n-attributes"

  }

}