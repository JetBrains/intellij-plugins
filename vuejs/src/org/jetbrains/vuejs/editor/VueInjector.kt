// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.editor

import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.lang.javascript.JSInjectionBracesUtil
import com.intellij.lang.javascript.JSInjectionBracesUtil.injectInXmlTextByDelimiters
import com.intellij.lang.javascript.index.JavaScriptIndex
import com.intellij.lang.javascript.psi.JSDefinitionExpression
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.impl.JSLiteralExpressionImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.xml.XmlAttributeValueImpl
import com.intellij.psi.impl.source.xml.XmlElementImpl
import com.intellij.psi.impl.source.xml.XmlTextImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.NullableFunction
import org.jetbrains.vuejs.codeInsight.EMPTY_FILTER
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.vuejs.codeInsight.es6Unquote
import org.jetbrains.vuejs.codeInsight.getStringLiteralsFromInitializerArray
import org.jetbrains.vuejs.index.VueOptionsIndex
import org.jetbrains.vuejs.index.isVueContext
import org.jetbrains.vuejs.index.resolve
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.model.source.VueComponents.Companion.onlyLocal
import java.util.*

class VueInjector : MultiHostInjector {
  companion object {
    private val delimitersOptionHolders = setOf("Vue.config.delimiters", "Vue.options.delimiters")

    val BRACES_FACTORY: NullableFunction<PsiElement, Pair<String, String>> = JSInjectionBracesUtil.delimitersFactory(
      VueJSLanguage.INSTANCE.displayName) { project, key ->
      if (project == null || key == null) return@delimitersFactory null
      calculateDelimitersFromIndex(project, key) ?: calculateDelimitersFromAssignment(project, key)
    }

    private fun calculateDelimitersFromIndex(project: Project, key: String): Pair<String, PsiElement>? {
      val elements = resolve("", GlobalSearchScope.projectScope(project), VueOptionsIndex.KEY) ?: return null
      val element = onlyLocal(elements).firstOrNull() ?: return null
      val obj = element as? JSObjectLiteralExpression
                ?: PsiTreeUtil.getParentOfType(element, JSObjectLiteralExpression::class.java)
                ?: return null
      return obj.findProperty("delimiters")
        ?.let { getDelimiterValue(it, key) }
        ?.let { Pair.create(it, element) }
    }

    private fun calculateDelimitersFromAssignment(project: Project, key: String): Pair<String, PsiElement>? {
      val delimitersDefinitions = JavaScriptIndex.getInstance(project).getSymbolsByName("delimiters", false)
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
      val list = getStringLiteralsFromInitializerArray(holder, EMPTY_FILTER)
      if (list.size != 2) return null
      val literal = list[if (JSInjectionBracesUtil.START_SYMBOL_KEY == key) 0 else 1] as? JSLiteralExpression ?: return null
      return es6Unquote(literal.significantValue!!)
    }
  }

  override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
    if (!isVueContext(context)) return

    val fileType = context.containingFile?.originalFile?.virtualFile?.fileType
    if (fileType != HtmlFileType.INSTANCE && fileType != VueFileType.INSTANCE) return

    // this supposed to work in <template lang="jade"> attribute values
    if (context is XmlAttributeValueImpl && !context.value.isBlank() && context.parent is XmlAttribute
        && VueAttributeNameParser.parse((context.parent as XmlAttribute).name, null).injectJS) {
      val embedded = PsiTreeUtil.getChildOfType(context, JSEmbeddedContent::class.java)
      if (embedded != null && VueJSLanguage.INSTANCE != embedded.language) {
        val literal = PsiTreeUtil.getChildOfType(embedded, JSLiteralExpressionImpl::class.java)
        if (literal != null) {
          injectInElement(literal, registrar)
          return
        }
      }
      else if (embedded == null) {
        injectInElement(context, registrar)
      }
    }

    if (context is XmlTextImpl || context is XmlAttributeValueImpl) {
      val braces = BRACES_FACTORY.`fun`(context) ?: return
      injectInXmlTextByDelimiters(registrar, context, VueJSLanguage.INSTANCE, braces.getFirst(), braces.getSecond())
    }
  }

  private fun injectInElement(host: PsiLanguageInjectionHost, registrar: MultiHostRegistrar) {
    registrar.startInjecting(VueJSLanguage.INSTANCE)
      .addPlace(null, null, host, ElementManipulators.getValueTextRange(host))
      .doneInjecting()
  }

  override fun elementsToInjectIn(): MutableList<out Class<out PsiElement>> {
    return Arrays.asList<Class<out XmlElementImpl>>(XmlTextImpl::class.java, XmlAttributeValueImpl::class.java)
  }
}
