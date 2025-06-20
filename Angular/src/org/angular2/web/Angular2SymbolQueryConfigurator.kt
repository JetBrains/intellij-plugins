// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web

import com.intellij.javascript.polySymbols.css.CssClassListInJSLiteralInHtmlAttributeScope
import com.intellij.javascript.polySymbols.css.CssClassListInJSLiteralInHtmlAttributeScope.Companion.isJSLiteralContextFromEmbeddedContent
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.context.PolyContext
import com.intellij.polySymbols.css.CSS_CLASS_LIST
import com.intellij.polySymbols.html.HTML_ATTRIBUTES
import com.intellij.polySymbols.html.NAMESPACE_HTML
import com.intellij.polySymbols.js.JS_PROPERTIES
import com.intellij.polySymbols.js.JS_STRING_LITERALS
import com.intellij.polySymbols.js.NAMESPACE_JS
import com.intellij.polySymbols.query.*
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.css.CssElement
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.parentOfTypes
import com.intellij.psi.util.siblings
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlElement
import com.intellij.psi.xml.XmlTag
import com.intellij.util.asSafely
import org.angular2.*
import org.angular2.Angular2DecoratorUtil.COMPONENT_DEC
import org.angular2.Angular2DecoratorUtil.DIRECTIVE_DEC
import org.angular2.Angular2DecoratorUtil.HOST_BINDING_DEC
import org.angular2.Angular2DecoratorUtil.VIEW_CHILDREN_DEC
import org.angular2.Angular2DecoratorUtil.VIEW_CHILD_DEC
import org.angular2.Angular2DecoratorUtil.getDecoratorForLiteralParameter
import org.angular2.Angular2DecoratorUtil.isHostBindingClassValueLiteral
import org.angular2.Angular2DecoratorUtil.isHostListenerDecoratorEventLiteral
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.codeInsight.attributes.isNgClassAttribute
import org.angular2.codeInsight.blocks.Angular2HtmlBlockReferenceExpressionCompletionProvider
import org.angular2.codeInsight.blocks.isDeferOnTriggerParameterReference
import org.angular2.codeInsight.blocks.isDeferOnTriggerReference
import org.angular2.codeInsight.blocks.isJSReferenceAfterEqInForBlockLetParameterAssignment
import org.angular2.entities.Angular2Directive
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.index.getFunctionNameFromIndex
import org.angular2.lang.expr.psi.*
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.lang.html.psi.Angular2HtmlBlock
import org.angular2.lang.html.psi.Angular2HtmlPropertyBinding
import org.angular2.signals.Angular2SignalUtils.getPossibleSignalFunNameForLiteralParameter
import org.angular2.signals.Angular2SignalUtils.isViewChildSignalCall
import org.angular2.signals.Angular2SignalUtils.isViewChildrenSignalCall
import org.angular2.web.scopes.*

class Angular2SymbolQueryScopeContributor : PolySymbolQueryScopeContributor {

  override fun registerProviders(registrar: PolySymbolQueryScopeProviderRegistrar) {
    registrar
      .inContext { it.framework == Angular2Framework.ID }
      .apply {

        forAnyPsiLocationInFile()
          .contributeScopeProvider {
            listOf(Angular2CustomCssPropertiesScope(it.containingFile))
          }

        forPsiLocations(XmlElement::class.java, CssElement::class.java)
          .contributeScopeProvider {
            listOf(DirectiveElementSelectorsScope(it.containingFile),
                   DirectiveAttributeSelectorsScope(it.containingFile))
          }

        forPsiLocation(CssElement::class.java)
          .contributeScopeProvider {
            listOf(HtmlAttributesCustomCssPropertiesScope(it))
          }

        forPsiLocations<XmlElement>(XmlAttributeValue::class.java, XmlAttribute::class.java, HtmlTag::class.java)
          .contributeScopeProvider(AngularTemplateXmlSymbolScopeProvider)

        forPsiLocation(Angular2HtmlPropertyBinding::class.java)
          .contributeScopeProvider {
            if (Angular2AttributeNameParser.parse(it.name).type == Angular2AttributeType.REGULAR)
              listOf(AttributeWithInterpolationsScope)
            else
              emptyList()
          }

        forPsiLocation(JSReferenceExpression::class.java)
          .contributeScopeProvider(JSReferenceSymbolScopeProvider)

        forPsiLocation(JSLiteralExpression::class.java)
          .contributeScopeProvider(JSLiteralExpressionScopeProvider)

        forPsiLocation(JSObjectLiteralExpression::class.java)
          .contributeScopeProvider(JSObjectLiteralExpressionScopeProvider)

        forPsiLocation(Angular2TemplateBindingKey::class.java)
          .contributeScopeProvider { element ->
            listOfNotNull(
              TemplateBindingKeyScope(element),
              TemplateBindingKeywordsScope.takeIf { (element.parent as? Angular2TemplateBinding)?.keyIsVar() == false }
            )
          }
      }
  }

  private object AngularTemplateXmlSymbolScopeProvider : PolySymbolLocationQueryScopeProvider<XmlElement> {
    override fun getScopes(location: XmlElement): List<PolySymbolScope> =
      location.parentOfType<XmlTag>(withSelf = true)?.let {
        listOf(
          OneTimeBindingsScope(it),
          StandardPropertyAndEventsScope(it.containingFile),
          NgContentSelectorsScope(it),
          MatchedDirectivesScope.createFor(it),
          I18NAttributesScope(it),
          HtmlAttributesCustomCssPropertiesScope(it),
        )
      } ?: emptyList()
  }

  private object JSReferenceSymbolScopeProvider : PolySymbolLocationQueryScopeProvider<JSReferenceExpression> {
    override fun getScopes(location: JSReferenceExpression): List<PolySymbolScope> =
      when {
        Angular2HtmlBlockReferenceExpressionCompletionProvider.canAddCompletions(location) ->
          emptyList()

        isJSReferenceAfterEqInForBlockLetParameterAssignment(location) ->
          listOfNotNull(location.parentOfType<Angular2HtmlBlock>()?.definition)

        isDeferOnTriggerReference(location) ->
          listOfNotNull(location.parentOfType<Angular2BlockParameter>()?.definition)

        isDeferOnTriggerParameterReference(location) ->
          listOfNotNull(location.parentOfType<Angular2BlockParameter>()?.let { DeferOnTriggerParameterScope(it) })

        isTemplateBindingKeywordLocation(location) ->
          listOf(TemplateBindingKeywordsScope)

        else ->
          listOfNotNull(DirectivePropertyMappingCompletionScope(location),
                        getCssClassesInJSLiteralInHtmlAttributeScope(location),
                        location.parentOfType<Angular2EmbeddedExpression>()?.let { Angular2TemplateScope(it) })
      }

    private fun isTemplateBindingKeywordLocation(element: JSReferenceExpression): Boolean =
      element.qualifier == null
      && element.parent is Angular2TemplateBinding
      && element.siblings(forward = false, withSelf = false).filter { it !is PsiWhiteSpace }.firstOrNull() !is Angular2TemplateBindingKey
  }

  private object JSObjectLiteralExpressionScopeProvider : PolySymbolLocationQueryScopeProvider<JSObjectLiteralExpression> {
    override fun getScopes(location: JSObjectLiteralExpression): List<PolySymbolScope> {
      var decorator: ES6Decorator? = null
      if (location.parent.asSafely<JSProperty>()?.name == Angular2DecoratorUtil.HOST_PROP
          && location.parentOfType<ES6Decorator>()
            ?.takeIf { Angular2DecoratorUtil.isAngularEntityDecorator(it, true, COMPONENT_DEC, DIRECTIVE_DEC) }
            ?.also { decorator = it } != null
      )
        return listOf(HostBindingsScope(mapOf(JS_PROPERTIES to HTML_ATTRIBUTES), decorator!!))
      else
        return listOfNotNull(getCssClassesInJSLiteralInHtmlAttributeScope(location))
    }
  }

  private object JSLiteralExpressionScopeProvider : PolySymbolLocationQueryScopeProvider<JSLiteralExpression> {
    override fun getScopes(location: JSLiteralExpression): List<PolySymbolScope> =
      listOfNotNull(
        DirectivePropertyMappingCompletionScope(location),
        getHostBindingsScopeForLiteral(location),
        getCssClassesInJSLiteralInHtmlAttributeScope(location),
        getViewChildrenScopeForLiteral(location),
        location.parentOfType<Angular2EmbeddedExpression>()?.let { Angular2TemplateScope(it) },
      ) + getCreateComponentBindingsScopeForLiteral(location)


    private fun getHostBindingsScopeForLiteral(element: JSLiteralExpression): PolySymbolScope? {
      val mapping = when {
        getDecoratorForLiteralParameter(element)?.decoratorName == HOST_BINDING_DEC -> NG_PROPERTY_BINDINGS
        isHostListenerDecoratorEventLiteral(element) -> NG_EVENT_BINDINGS
        isHostBindingClassValueLiteral(element) -> CSS_CLASS_LIST
        else -> return null
      }

      return element
        .parentOfType<TypeScriptClass>()
        ?.let { Angular2DecoratorUtil.findDecorator(it, true, COMPONENT_DEC, DIRECTIVE_DEC) }
        ?.let { HostBindingsScope(mapOf(JS_STRING_LITERALS to mapping), it) }
    }

    private fun getViewChildrenScopeForLiteral(element: JSLiteralExpression): PolySymbolScope? {
      val signalCallInfo = getPossibleSignalFunNameForLiteralParameter(element)
      val decorator = getDecoratorForLiteralParameter(element)
      val isChildViewCall = decorator?.decoratorName == VIEW_CHILD_DEC || isViewChildSignalCall(signalCallInfo)
      val isChildrenViewCall = decorator?.decoratorName == VIEW_CHILDREN_DEC || isViewChildrenSignalCall(signalCallInfo)
      if (!isChildViewCall && !isChildrenViewCall) return null

      return element
        .parentOfType<TypeScriptClass>()
        ?.let { Angular2DecoratorUtil.findDecorator(it, true, COMPONENT_DEC, DIRECTIVE_DEC) }
        ?.let { ViewChildrenScope(it, isChildrenViewCall) }
    }

    private fun getCreateComponentBindingsScopeForLiteral(element: JSLiteralExpression): List<PolySymbolScope> {
      val callExpr =
        element.context
          ?.let { if (it is JSArgumentList) it.parent else it }
          ?.asSafely<JSCallExpression>()
        ?: return emptyList()
      val functionName = getFunctionNameFromIndex(callExpr)
                           ?.takeIf { it == TWO_WAY_BINDING_FUN || it == OUTPUT_BINDING_FUN || it == INPUT_BINDING_FUN }
                         ?: return emptyList()
      val objectLiteral =
        callExpr.context
          ?.let { if (it is JSArrayLiteralExpression) it.parent else it }
          ?.asSafely<JSProperty>()
          ?.takeIf { it.name == BINDINGS_PROP }
          ?.context?.asSafely<JSObjectLiteralExpression>()
        ?: return emptyList()
      return listOf(
        CreateComponentDirectiveBindingScope(objectLiteral),
        when (functionName) {
          INPUT_BINDING_FUN -> CreateComponentDirectiveBindingScope.INPUTS_SCOPE
          OUTPUT_BINDING_FUN -> CreateComponentDirectiveBindingScope.OUTPUTS_SCOPE
          TWO_WAY_BINDING_FUN -> CreateComponentDirectiveBindingScope.IN_OUTS_SCOPE
          else -> throw IllegalStateException("Unexpected function name: $functionName")
        }
      )
    }
  }
}


class Angular2SymbolQueryConfigurator : PolySymbolQueryConfigurator {

  override fun getNameConversionRulesProviders(project: Project, element: PsiElement?, context: PolyContext): List<PolySymbolNameConversionRulesProvider> {
    if (context.framework == Angular2Framework.ID && element != null) {
      // possibly the input definition
      if (element is JSLiteralExpression || element is TypeScriptField) {
        val parent = element
          .parentOfTypes(TypeScriptClass::class, ES6Decorator::class)
          ?.let {
            if (it is ES6Decorator && !Angular2DecoratorUtil.isAngularEntityDecorator(it, true, COMPONENT_DEC, DIRECTIVE_DEC))
              it.parentOfType<TypeScriptClass>()
            else
              it
          }
        if (parent == null || parent is TypeScriptClass && Angular2DecoratorUtil.findDecorator(parent, false, COMPONENT_DEC, DIRECTIVE_DEC) == null)
          return emptyList()
        val attrSelectors by lazy(LazyThreadSafetyMode.PUBLICATION) {
          Angular2EntitiesProvider.getDirective(parent)?.selector
            ?.simpleSelectors
            ?.flatMapTo(mutableSetOf()) { it.attrNames }
          ?: emptyList()
        }
        return listOf(object : PolySymbolNameConversionRulesProvider {
          override fun getNameConversionRules(): PolySymbolNameConversionRules =
            PolySymbolNameConversionRules.builder()
              .addMatchNamesRule(NG_DIRECTIVE_INPUTS) { name ->
                attrSelectors.mapNotNull {
                  if (isTemplateBindingDirectiveInput(name, it))
                    directiveInputToTemplateBindingVar(name, it)
                  else null
                } + name
              }
              .build()

          override fun createPointer(): Pointer<out PolySymbolNameConversionRulesProvider> =
            Pointer.hardPointer(this)

          override fun getModificationCount(): Long = 0
        })
      }
      else if (element is Angular2TemplateBindingKey) {
        val templateName = element.parentOfType<Angular2TemplateBindings>()?.templateName
        if (templateName != null)
          return listOf(object : PolySymbolNameConversionRulesProvider {
            override fun getNameConversionRules(): PolySymbolNameConversionRules =
              PolySymbolNameConversionRules.builder()
                .addMatchNamesRule(NG_DIRECTIVE_INPUTS) {
                  listOf(templateBindingVarToDirectiveInput(it, templateName))
                }
                .addRenameRule(NG_DIRECTIVE_INPUTS) {
                  listOf(directiveInputToTemplateBindingVar(it, templateName))
                }
                .addCanonicalNamesRule(NG_DIRECTIVE_OUTPUTS) {
                  listOf(templateBindingVarToDirectiveInput(it, templateName))
                }
                .addCompletionVariantsRule(NG_DIRECTIVE_INPUTS) {
                  listOf(directiveInputToTemplateBindingVar(it, templateName))
                }
                .build()

            override fun createPointer(): Pointer<out PolySymbolNameConversionRulesProvider> =
              Pointer.hardPointer(this)

            override fun getModificationCount(): Long = 0
          })
      }
    }
    return emptyList()
  }

}

private fun getCssClassesInJSLiteralInHtmlAttributeScope(element: PsiElement): PolySymbolScope? =
  element.takeIf { isNgClassLiteralContext(it) }
    ?.parentOfType<XmlAttribute>()
    ?.let { CssClassListInJSLiteralInHtmlAttributeScope(it) }

@JvmField
val PROP_BINDING_PATTERN: PolySymbolProperty<Boolean> = PolySymbolProperty["ng-binding-pattern"]

@JvmField
val PROP_ERROR_SYMBOL: PolySymbolProperty<Boolean> = PolySymbolProperty["ng-error-symbol"]

@JvmField
val PROP_SYMBOL_DIRECTIVE: PolySymbolProperty<Angular2Directive> = PolySymbolProperty["ng-symbol-directive"]

@JvmField
val PROP_SCOPE_PROXIMITY: PolySymbolProperty<Angular2DeclarationsScope.DeclarationProximity> = PolySymbolProperty["scope-proximity"]

@JvmField
val PROP_HOST_BINDING: PolySymbolProperty<Boolean> = PolySymbolProperty["ng-host-binding"]

const val EVENT_ATTR_PREFIX: String = "on"

const val ATTR_NG_NON_BINDABLE: String = "ngNonBindable"
const val ATTR_SELECT: String = "select"

const val ELEMENT_NG_CONTAINER: String = "ng-container"
const val ELEMENT_NG_CONTENT: String = "ng-content"
const val ELEMENT_NG_TEMPLATE: String = "ng-template"

val NG_PROPERTY_BINDINGS: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_HTML, "ng-property-bindings"]
val NG_EVENT_BINDINGS: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_HTML, "ng-event-bindings"]
val NG_STRUCTURAL_DIRECTIVES: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_JS, "ng-structural-directives"]
val NG_DIRECTIVE_ONE_TIME_BINDINGS: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_JS, "ng-one-time-bindings"]
val NG_DIRECTIVE_INPUTS: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_JS, "ng-directive-inputs"]
val NG_DIRECTIVE_OUTPUTS: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_JS, "ng-directive-outputs"]
val NG_DIRECTIVE_IN_OUTS: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_JS, "ng-directive-in-outs"]
val NG_DIRECTIVE_ATTRIBUTES: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_JS, "ng-directive-attributes"]
val NG_DIRECTIVE_EXPORTS_AS: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_JS, "ng-directive-exports-as"]
val NG_DIRECTIVE_ELEMENT_SELECTORS: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_JS, "ng-directive-element-selectors"]
val NG_DIRECTIVE_ATTRIBUTE_SELECTORS: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_JS, "ng-directive-attribute-selectors"]
val NG_I18N_ATTRIBUTES: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_HTML, "ng-i18n-attributes"]
val NG_BLOCKS: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_HTML, "ng-blocks"]
val NG_BLOCK_PARAMETERS: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_HTML, "ng-block-parameters"]
val NG_BLOCK_PARAMETER_PREFIXES: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_HTML, "ng-block-parameter-prefixes"]
val NG_DEFER_ON_TRIGGERS: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_JS, "ng-defer-on-triggers"]
val NG_TEMPLATE_BINDING_KEYWORDS: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_JS, "ng-template-binding-keywords"]
val NG_TEMPLATE_BINDINGS: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_JS, "ng-template-bindings"]

fun isNgClassLiteralContext(literal: PsiElement): Boolean =
  isJSLiteralContextFromEmbeddedContent(literal, Angular2Binding::class.java, ::isNgClassAttribute)
