// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web

import com.intellij.webSymbols.registry.WebSymbolsRegistryExtension
import com.intellij.webSymbols.WebSymbolsContainer
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.webSymbols.context.WebSymbolsContext
import org.angular2.Angular2Framework
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.lang.html.psi.Angular2HtmlPropertyBinding
import org.angular2.web.containers.*

class Angular2WebSymbolsRegistryExtension : WebSymbolsRegistryExtension {

  override fun getContainers(project: Project,
                             element: PsiElement?,
                             context: WebSymbolsContext,
                             allowResolve: Boolean): List<WebSymbolsContainer> =
    if (context.framework == Angular2Framework.ID && element != null) {
      val result = mutableListOf(DirectiveElementSelectorsContainer(element.project),
                                 DirectiveAttributeSelectorsContainer(element.project))
      ((element as? XmlAttribute)?.parent ?: element as? XmlTag)?.let {
        result.addAll(listOf(
          OneTimeBindingsProvider(),
          StandardPropertyAndEventsContainer(it.containingFile),
          NgContentSelectorsContainer(it),
          MatchedDirectivesContainer(it),
          I18nAttributesContainer(it),
        ))
      }
      if (element is Angular2HtmlPropertyBinding
          && Angular2AttributeNameParser.parse(element.name).type == Angular2AttributeType.REGULAR) {
        result.add(AttributeWithInterpolationsContainer)
      }
      result
    }
    else emptyList()

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