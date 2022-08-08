// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.editor

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.lang.javascript.JSInjectionBracesUtil
import com.intellij.lang.javascript.JSInjectionBracesUtil.injectInXmlTextByDelimiters
import com.intellij.lang.javascript.index.JavaScriptIndex
import com.intellij.lang.javascript.injections.JSFormattableInjectionUtil
import com.intellij.lang.javascript.injections.JSInjectionUtil
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.impl.JSLiteralExpressionImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.xml.XmlAttributeValueImpl
import com.intellij.psi.impl.source.xml.XmlTextImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.NullableFunction
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.vuejs.codeInsight.es6Unquote
import org.jetbrains.vuejs.codeInsight.getStringLiteralsFromInitializerArray
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.index.VueFrameworkHandler
import org.jetbrains.vuejs.index.VueOptionsIndex
import org.jetbrains.vuejs.index.resolve
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.expr.parser.VueJSParserDefinition
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.jetbrains.vuejs.lang.html.parser.VueFileElementType.Companion.INJECTED_FILE_SUFFIX
import org.jetbrains.vuejs.libraries.componentDecorator.isComponentDecorator
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueRegularComponent
import org.jetbrains.vuejs.model.source.DELIMITERS_PROP
import org.jetbrains.vuejs.model.source.TEMPLATE_PROP
import org.jetbrains.vuejs.model.source.VueComponents.Companion.onlyLocal
import org.jetbrains.vuejs.model.source.VueSourceContainer

class VueInjector : MultiHostInjector {
  internal object Holder {
    private val delimitersOptionHolders = setOf("Vue.config.delimiters", "Vue.options.delimiters")

    val BRACES_FACTORY: NullableFunction<PsiElement, Pair<String, String>> = JSInjectionBracesUtil.delimitersFactory(
      VueJSLanguage.INSTANCE.displayName,
      { element ->
        (VueModelManager.findEnclosingContainer(element) as? VueSourceContainer)
          ?.delimiters
          ?.let {
            Pair(it.first, it.second)
          }
      }, { project, key ->
        if (project == null || key == null) return@delimitersFactory null
        calculateDelimitersFromIndex(project, key) ?: calculateDelimitersFromAssignment(project, key)
      })

    private fun calculateDelimitersFromIndex(project: Project, key: String): Pair<String, PsiElement>? {
      val elements = resolve("", GlobalSearchScope.projectScope(project), VueOptionsIndex.KEY)
        .ifEmpty { return null }
      val element = onlyLocal(elements).firstOrNull() ?: return null
      val obj = element as? JSObjectLiteralExpression
                ?: PsiTreeUtil.getParentOfType(element, JSObjectLiteralExpression::class.java)
                ?: return null
      return obj.findProperty(DELIMITERS_PROP)
        ?.let { getDelimiterValue(it, key) }
        ?.let { Pair.create(it, element) }
    }

    private fun calculateDelimitersFromAssignment(project: Project, key: String): Pair<String, PsiElement>? {
      val delimitersDefinitions = JavaScriptIndex.getInstance(project).getSymbolsByName(DELIMITERS_PROP, false)
      return delimitersDefinitions.filter {
        it is JSDefinitionExpression &&
        (it as PsiElement).context != null &&
        it.qualifiedName in delimitersOptionHolders
      }.map {
        val delimiter = getDelimiterValue((it as PsiElement).context!!, key)
        if (delimiter != null) return Pair.create(delimiter, it)
        return null
      }.firstOrNull()
    }

    private fun getDelimiterValue(holder: PsiElement, key: String): String? {
      val list = getStringLiteralsFromInitializerArray(holder)
      if (list.size != 2) return null
      val literal = list[if (JSInjectionBracesUtil.START_SYMBOL_KEY == key) 0 else 1] as? JSLiteralExpression ?: return null
      return es6Unquote(literal.significantValue!!)
    }
  }

  override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
    val parent = context.parent
    if (parent == null || !isVueContext(context)) return

    // this supposed to work in <template lang="jade"> attribute values
    if (context is XmlAttributeValueImpl
        && context.value.isNotBlank()
        && parent is XmlAttribute
        && parent.parent != null
        && VueAttributeNameParser.parse(parent.name, parent.parent).injectJS) {
      if (parent.language !== VueLanguage.INSTANCE) {
        val embedded = PsiTreeUtil.getChildOfType(context, JSEmbeddedContent::class.java)
        if (embedded != null) {
          val literal = PsiTreeUtil.getChildOfType(embedded, JSLiteralExpressionImpl::class.java)
          if (literal != null && literal.isValidHost) {
            injectInElement(literal, registrar, parent.name)
          }
        }
        else {
          injectInElement(context, registrar, parent.name)
        }
      }
      return
    }

    if (context is XmlTextImpl || context is XmlAttributeValueImpl) {
      val braces = Holder.BRACES_FACTORY.`fun`(context) ?: return
      injectInXmlTextByDelimiters(registrar, context, VueJSLanguage.INSTANCE,
                                  braces.getFirst(), braces.getSecond(),
                                  VueJSParserDefinition.INTERPOLATION)
    }
    else if (context is JSLiteralExpressionImpl
             && context.isQuotedLiteral
             && context.isValidHost
             && parent is JSProperty
             && parent.name == TEMPLATE_PROP
             && parent.parent is JSObjectLiteralExpression
             && shouldInjectVueTemplate(context, parent.parent as JSObjectLiteralExpression)) {

      val braces = (VueModelManager.getComponent(parent.parent) as? VueRegularComponent)
                     ?.delimiters
                     ?.let {
                       Pair(it.first, it.second)
                     }
                   ?: Holder.BRACES_FACTORY.`fun`(context)
                   ?: Pair(JSInjectionBracesUtil.DEFAULT_START, JSInjectionBracesUtil.DEFAULT_END)
      JSInjectionUtil.injectInQuotedLiteral(registrar,
                                            VueLanguage.INSTANCE,
                                            "${braces.first}.${braces.second}${INJECTED_FILE_SUFFIX}",
                                            context, null, null)
      JSFormattableInjectionUtil.setReformattableInjection(context, VueLanguage.INSTANCE)
    }

  }

  private fun shouldInjectVueTemplate(template: JSLiteralExpressionImpl, initializer: JSObjectLiteralExpression): Boolean {
    val chars = template.node.chars
    if (chars.length > 2 && chars[1] == '#')
      return false
    return VueFrameworkHandler.hasComponentIndicatorProperties(initializer, TEMPLATE_PROP)
           || PsiTreeUtil.getContextOfType(initializer, ES6Decorator::class.java)
             ?.let { isComponentDecorator(it) } == true
  }

  private fun injectInElement(host: PsiLanguageInjectionHost,
                              registrar: MultiHostRegistrar,
                              attributeName: String) {
    registrar.startInjecting(VueJSLanguage.INSTANCE, "${attributeName.replace('.', ' ')}.${VueJSParserDefinition.EXPRESSION}")
      .addPlace(null, null, host, ElementManipulators.getValueTextRange(host))
      .doneInjecting()
  }

  override fun elementsToInjectIn(): List<Class<out PsiElement>> {
    return listOf(XmlTextImpl::class.java,
                  XmlAttributeValueImpl::class.java,
                  JSLiteralExpressionImpl::class.java)
  }
}
