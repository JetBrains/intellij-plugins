// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.css.CssElement
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlElement
import com.intellij.psi.xml.XmlTag
import com.intellij.util.asSafely
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_CSS
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_HTML
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_JS
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.WebSymbolsScope
import com.intellij.webSymbols.context.WebSymbolsContext
import com.intellij.webSymbols.query.WebSymbolsQueryConfigurator
import org.angular2.Angular2DecoratorUtil
import org.angular2.Angular2DecoratorUtil.COMPONENT_DEC
import org.angular2.Angular2DecoratorUtil.DIRECTIVE_DEC
import org.angular2.Angular2DecoratorUtil.isHostBindingClassValueLiteral
import org.angular2.Angular2DecoratorUtil.isHostBindingDecoratorLiteral
import org.angular2.Angular2DecoratorUtil.isHostListenerDecoratorEventLiteral
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

  override fun getScope(
    project: Project,
    location: PsiElement?,
    context: WebSymbolsContext,
    allowResolve: Boolean,
  ): List<WebSymbolsScope> =
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
    val result = mutableListOf(DirectiveElementSelectorsScope(element.containingFile),
                               DirectiveAttributeSelectorsScope(element.containingFile))

    if (element is XmlAttributeValue || element is XmlAttribute || element is XmlTag) {
      element.parentOfType<XmlTag>(withSelf = true)?.let {
        result.addAll(listOf(
          OneTimeBindingsScope(it),
          StandardPropertyAndEventsScope(it.containingFile),
          NgContentSelectorsScope(it),
          MatchedDirectivesScope.createFor(it),
          I18NAttributesScope(it),
        ))
      }
    }
    if (element is Angular2HtmlPropertyBinding
        && Angular2AttributeNameParser.parse(element.name).type == Angular2AttributeType.REGULAR) {
      result.add(AttributeWithInterpolationsScope)
    }
    return result
  }

  private fun calculateCssScopes(element: CssElement): List<WebSymbolsScope> =
    listOf(DirectiveElementSelectorsScope(element.containingFile),
           DirectiveAttributeSelectorsScope(element.containingFile))

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
                      getHostBindingsScopeForLiteral(element),
                      element.parentOfType<Angular2EmbeddedExpression>()?.let { WebSymbolsTemplateScope(it) })
      }
      is JSObjectLiteralExpression -> {
        var decorator: ES6Decorator? = null
        if (element.parent.asSafely<JSProperty>()?.name == Angular2DecoratorUtil.HOST_PROP
            && element.parentOfType<ES6Decorator>()
              ?.takeIf { Angular2DecoratorUtil.isAngularEntityDecorator(it, true, COMPONENT_DEC, DIRECTIVE_DEC) }
              ?.also { decorator = it } != null
        )
          listOf(HostBindingsScope(mapOf(WebSymbol.JS_PROPERTIES to WebSymbol.HTML_ATTRIBUTES), decorator!!))
        else
          emptyList()
      }
      else -> emptyList()
    }

  private fun getHostBindingsScopeForLiteral(element: JSLiteralExpression): WebSymbolsScope? {
    val mapping = when {
      isHostBindingDecoratorLiteral(element) -> NG_PROPERTY_BINDINGS
      isHostListenerDecoratorEventLiteral(element) -> NG_EVENT_BINDINGS
      isHostBindingClassValueLiteral(element) -> NG_CLASS_LIST
      else -> return null
    }

    return element
      .parentOfType<TypeScriptClass>()
      ?.let { Angular2DecoratorUtil.findDecorator(it, true, COMPONENT_DEC, DIRECTIVE_DEC) }
      ?.let { HostBindingsScope(mapOf(WebSymbol.JS_STRING_LITERALS to mapping), it) }
  }
}

const val PROP_BINDING_PATTERN: String = "ng-binding-pattern"
const val PROP_ERROR_SYMBOL: String = "ng-error-symbol"
const val PROP_SYMBOL_DIRECTIVE: String = "ng-symbol-directive"
const val PROP_SCOPE_PROXIMITY: String = "scope-proximity"
const val PROP_HOST_BINDING: String = "ng-host-binding"

const val EVENT_ATTR_PREFIX: String = "on"

const val ATTR_NG_NON_BINDABLE: String = "ngNonBindable"
const val ATTR_SELECT: String = "select"

const val ELEMENT_NG_CONTAINER: String = "ng-container"
const val ELEMENT_NG_CONTENT: String = "ng-content"
const val ELEMENT_NG_TEMPLATE: String = "ng-template"

val NG_PROPERTY_BINDINGS: WebSymbolQualifiedKind = WebSymbolQualifiedKind(NAMESPACE_HTML, "ng-property-bindings")
val NG_EVENT_BINDINGS: WebSymbolQualifiedKind = WebSymbolQualifiedKind(NAMESPACE_HTML, "ng-event-bindings")
val NG_CLASS_LIST: WebSymbolQualifiedKind = WebSymbolQualifiedKind(NAMESPACE_CSS, "ng-class-list")
val NG_STRUCTURAL_DIRECTIVES: WebSymbolQualifiedKind = WebSymbolQualifiedKind(NAMESPACE_JS, "ng-structural-directives")
val NG_DIRECTIVE_ONE_TIME_BINDINGS: WebSymbolQualifiedKind = WebSymbolQualifiedKind(NAMESPACE_JS, "ng-one-time-bindings")
val NG_DIRECTIVE_INPUTS: WebSymbolQualifiedKind = WebSymbolQualifiedKind(NAMESPACE_JS, "ng-directive-inputs")
val NG_DIRECTIVE_OUTPUTS: WebSymbolQualifiedKind = WebSymbolQualifiedKind(NAMESPACE_JS, "ng-directive-outputs")
val NG_DIRECTIVE_IN_OUTS: WebSymbolQualifiedKind = WebSymbolQualifiedKind(NAMESPACE_JS, "ng-directive-in-outs")
val NG_DIRECTIVE_ATTRIBUTES: WebSymbolQualifiedKind = WebSymbolQualifiedKind(NAMESPACE_JS, "ng-directive-attributes")
val NG_DIRECTIVE_EXPORTS_AS: WebSymbolQualifiedKind = WebSymbolQualifiedKind(NAMESPACE_JS, "ng-directive-exports-as")
val NG_DIRECTIVE_ELEMENT_SELECTORS: WebSymbolQualifiedKind = WebSymbolQualifiedKind(NAMESPACE_JS, "ng-directive-element-selectors")
val NG_DIRECTIVE_ATTRIBUTE_SELECTORS: WebSymbolQualifiedKind = WebSymbolQualifiedKind(NAMESPACE_JS, "ng-directive-attribute-selectors")
val NG_I18N_ATTRIBUTES: WebSymbolQualifiedKind = WebSymbolQualifiedKind(NAMESPACE_HTML, "ng-i18n-attributes")
val NG_BLOCKS: WebSymbolQualifiedKind = WebSymbolQualifiedKind(NAMESPACE_HTML, "ng-blocks")
val NG_BLOCK_PARAMETERS: WebSymbolQualifiedKind = WebSymbolQualifiedKind(NAMESPACE_HTML, "ng-block-parameters")
val NG_BLOCK_PARAMETER_PREFIXES: WebSymbolQualifiedKind = WebSymbolQualifiedKind(NAMESPACE_HTML, "ng-block-parameter-prefixes")
val NG_DEFER_ON_TRIGGERS: WebSymbolQualifiedKind = WebSymbolQualifiedKind(NAMESPACE_JS, "ng-defer-on-triggers")