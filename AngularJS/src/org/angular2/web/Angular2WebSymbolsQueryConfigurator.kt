// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web

import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.css.CssElement
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlElement
import com.intellij.psi.xml.XmlTag
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_HTML
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_JS
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.WebSymbolsScope
import com.intellij.webSymbols.context.WebSymbolsContext
import com.intellij.webSymbols.query.WebSymbolsQueryConfigurator
import org.angular2.Angular2Framework
import org.angular2.codeInsight.blocks.Angular2HtmlBlockReferenceExpressionCompletionProvider
import org.angular2.codeInsight.blocks.isDeferOnTriggerParameterReference
import org.angular2.codeInsight.blocks.isDeferOnTriggerReference
import org.angular2.codeInsight.blocks.isJSReferenceAfterEqInForBlockLetParameterAssignment
import org.angular2.lang.expr.psi.Angular2BlockParameter
import org.angular2.lang.expr.psi.Angular2EmbeddedExpression
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.lang.html.psi.Angular2HtmlBlock
import org.angular2.lang.html.psi.Angular2HtmlPropertyBinding
import org.angular2.web.scopes.*

class Angular2WebSymbolsQueryConfigurator : WebSymbolsQueryConfigurator {

  override fun getScope(project: Project,
                        location: PsiElement?,
                        context: WebSymbolsContext,
                        allowResolve: Boolean): List<WebSymbolsScope> =
    if (context.framework == Angular2Framework.ID && location != null) {
      when (location) {
        is JSElement -> calculateJavaScriptScopes(location)
        is XmlElement -> calculateHtmlScopes(location)
        is CssElement -> calculateCssScopes(location)
        else -> emptyList()
      }
    }
    else emptyList()

  private fun calculateHtmlScopes(element: XmlElement): MutableList<WebSymbolsScope> {
    val result = mutableListOf(DirectiveElementSelectorsScope(element.project),
                               DirectiveAttributeSelectorsScope(element.project))
    ((element as? XmlAttribute)?.parent ?: element as? XmlTag)?.let {
      result.addAll(listOf(
        OneTimeBindingsScope(it),
        StandardPropertyAndEventsScope(it.containingFile),
        NgContentSelectorsScope(it),
        MatchedDirectivesScope(it),
        I18NAttributesScope(it),
      ))
    }
    if (element is Angular2HtmlPropertyBinding
        && Angular2AttributeNameParser.parse(element.name).type == Angular2AttributeType.REGULAR) {
      result.add(AttributeWithInterpolationsScope)
    }
    return result
  }

  private fun calculateCssScopes(element: CssElement): List<WebSymbolsScope> =
    listOf(DirectiveElementSelectorsScope(element.project),
           DirectiveAttributeSelectorsScope(element.project))

  private fun calculateJavaScriptScopes(element: JSElement): List<WebSymbolsScope> =
    when (element) {
      is JSReferenceExpression -> {
        when {
          Angular2HtmlBlockReferenceExpressionCompletionProvider.canAddCompletions(element) ->
            emptyList()

          isJSReferenceAfterEqInForBlockLetParameterAssignment(element) ->
            listOfNotNull(element.parentOfType<Angular2HtmlBlock>()?.definition)

          isDeferOnTriggerReference(element) ->
            listOfNotNull(element.parentOfType<Angular2BlockParameter>()?.definition)

          isDeferOnTriggerParameterReference(element) ->
            listOfNotNull(element.parentOfType<Angular2BlockParameter>()?.let { DeferOnTriggerParameterScope(it) })

          else ->
            listOfNotNull(DirectivePropertyMappingCompletionScope(element),
                          element.parentOfType<Angular2EmbeddedExpression>()?.let { WebSymbolsTemplateScope(it) })
        }
      }
      is JSLiteralExpression -> {
        listOfNotNull(DirectivePropertyMappingCompletionScope(element),
                      element.parentOfType<Angular2EmbeddedExpression>()?.let { WebSymbolsTemplateScope(it) })
      }
      else -> emptyList()
    }


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

    val NG_PROPERTY_BINDINGS = WebSymbolQualifiedKind(NAMESPACE_HTML, "ng-property-bindings")
    val NG_STRUCTURAL_DIRECTIVES = WebSymbolQualifiedKind(NAMESPACE_JS, "ng-structural-directives")

    val NG_DIRECTIVE_ONE_TIME_BINDINGS = WebSymbolQualifiedKind(NAMESPACE_JS, "ng-one-time-bindings")
    val NG_DIRECTIVE_INPUTS = WebSymbolQualifiedKind(NAMESPACE_JS, "ng-directive-inputs")
    val NG_DIRECTIVE_OUTPUTS = WebSymbolQualifiedKind(NAMESPACE_JS, "ng-directive-outputs")
    val NG_DIRECTIVE_IN_OUTS = WebSymbolQualifiedKind(NAMESPACE_JS, "ng-directive-in-outs")
    val NG_DIRECTIVE_ATTRIBUTES = WebSymbolQualifiedKind(NAMESPACE_JS, "ng-directive-attributes")

    val NG_DIRECTIVE_EXPORTS_AS = WebSymbolQualifiedKind(NAMESPACE_JS, "ng-directive-exports-as")

    val NG_DIRECTIVE_ELEMENT_SELECTORS = WebSymbolQualifiedKind(NAMESPACE_JS, "ng-directive-element-selectors")
    val NG_DIRECTIVE_ATTRIBUTE_SELECTORS = WebSymbolQualifiedKind(NAMESPACE_JS, "ng-directive-attribute-selectors")

    val NG_I18N_ATTRIBUTES = WebSymbolQualifiedKind(NAMESPACE_HTML, "ng-i18n-attributes")

    val NG_BLOCKS = WebSymbolQualifiedKind(NAMESPACE_HTML, "ng-blocks")
    val NG_BLOCK_PARAMETERS = WebSymbolQualifiedKind(NAMESPACE_HTML, "ng-block-parameters")
    val NG_DEFER_ON_TRIGGERS = WebSymbolQualifiedKind(NAMESPACE_JS, "ng-defer-on-triggers")

  }

}