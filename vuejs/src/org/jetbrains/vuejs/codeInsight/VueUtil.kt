// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.types.JSCompositeTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeContext
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.lang.javascript.psi.types.primitives.JSBooleanType
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ObjectUtils.tryCast
import one.util.streamex.StreamEx
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.index.findScriptTag
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpression
import org.jetbrains.vuejs.lang.html.VueLanguage

fun fromAsset(text: String): String {
  val split = es6Unquote(text).split("(?=[A-Z])".toRegex()).filter { !StringUtil.isEmpty(it) }.toTypedArray()
  for (i in split.indices) {
    split[i] = StringUtil.decapitalize(split[i])
  }
  return StringUtil.join(split, "-")
}

fun toAsset(name: String): String {
  val words = name.split("-".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
  for (i in 1 until words.size) {
    words[i] = StringUtil.capitalize(words[i])
  }
  return StringUtil.join(*words)
}

private val QUOTES = setOf('\'', '"', '`')
fun es6Unquote(s: String): String {
  if (s.length < 2) return s
  if (QUOTES.contains(s[0]) && s.endsWith(s[0])) return s.substring(1, s.length - 1)
  return s
}

val EMPTY_FILTER: (String, PsiElement) -> Boolean = { _, _ -> true }
fun getStringLiteralsFromInitializerArray(holder: PsiElement,
                                          filter: (String, PsiElement) -> Boolean): List<JSLiteralExpression> {
  return JSStubBasedPsiTreeUtil.findDescendants<JSLiteralExpression>(holder,
                                                                     TokenSet.create(JSStubElementTypes.LITERAL_EXPRESSION,
                                                                                     JSStubElementTypes.STRING_TEMPLATE_EXPRESSION))
    .filter {
      val context = it.context
      !it.significantValue.isNullOrBlank() &&
      QUOTES.contains(it.significantValue!![0]) &&
      filter(es6Unquote(it.significantValue!!), it) &&
      ((context is JSArrayLiteralExpression) && (context.parent == holder) || context == holder)
    }
}

fun getTextIfLiteral(holder: PsiElement?): String? {
  if (holder != null && holder is JSLiteralExpression && holder.isQuotedLiteral) {
    return holder.stringValue
  }
  return null
}

fun detectLanguage(tag: XmlTag?): String? = tag?.getAttribute("lang")?.value?.trim()

fun detectVueScriptLanguage(file: PsiFile): String? {
  val xmlFile = file as? XmlFile ?: return null
  val scriptTag = findScriptTag(xmlFile) ?: return null
  return detectLanguage(scriptTag)
}

val BOOLEAN_TYPE = JSBooleanType(true, JSTypeSource.EXPLICITLY_DECLARED, JSTypeContext.INSTANCE)

fun getJSTypeFromPropOptions(expression: JSExpression?): JSType? {
  return when (expression) {
    is JSReferenceExpression -> getJSTypeFromVueType(expression)
    is JSArrayLiteralExpression -> JSCompositeTypeImpl.getCommonType(
      StreamEx.of(*expression.expressions)
        .select(JSReferenceExpression::class.java)
        .map { getJSTypeFromVueType(it) }
        .nonNull()
        .toList(),
      JSTypeSource.EXPLICITLY_DECLARED, false
    )
    is JSObjectLiteralExpression -> expression.findProperty("type")
      ?.value
      ?.let {
        when (it) {
          is JSReferenceExpression -> getJSTypeFromVueType(it)
          is JSArrayLiteralExpression -> getJSTypeFromPropOptions(it)
          else -> null
        }
      }
    else -> null
  }
}

private fun getJSTypeFromVueType(reference: JSReferenceExpression): JSType? {
  return reference
    .referenceName
    // TODO support other types here
    ?.let { name -> if (name == "Boolean") BOOLEAN_TYPE else null }
}

fun getRequiredFromPropOptions(expression: JSExpression?): Boolean {
  return (expression as? JSObjectLiteralExpression)
           ?.findProperty("required")
           ?.literalExpressionInitializer
           ?.let {
             it.isBooleanLiteral && "true" == it.significantValue
           }
         ?: false
}

fun createContainingFileScope(directives: JSProperty?): GlobalSearchScope? {
  directives ?: return null
  val file = getContainingXmlFile(directives) ?: return null
  return GlobalSearchScope.fileScope(file.originalFile)
}

fun <T : JSExpression> findExpressionInAttributeValue(attribute: XmlAttribute,
                                                      expressionClass: Class<T>): T? {
  val value = attribute.valueElement ?: return null

  val root = when {
    attribute.language === VueLanguage.INSTANCE ->
      value.children
        .find { it is ASTWrapperPsiElement }
    value.textLength >= 2 ->
      InjectedLanguageManager.getInstance(attribute.project).findInjectedElementAt(
        value.containingFile, value.textOffset + 1)
        ?.containingFile
    else -> null
  }

  return tryCast((root?.firstChild as? VueJSEmbeddedExpression)?.firstChild, expressionClass)
}

fun getFirstInjectedFile(element: PsiElement?): PsiFile? {
  return element
    ?.let { InjectedLanguageManager.getInstance(element.project).getInjectedPsiFiles(element) }
    ?.asSequence()
    ?.mapNotNull { it.first as? PsiFile }
    ?.first()
}

fun findScriptWithExport(element: PsiElement): Pair<PsiElement, ES6ExportDefaultAssignment>? {
  val xmlFile = getContainingXmlFile(element) ?: return null

  val module = findModule(xmlFile) ?: return null
  val defaultExport = ES6PsiUtil.findDefaultExport(module)
                        as? ES6ExportDefaultAssignment ?: return null
  if (defaultExport.stubSafeElement is JSObjectLiteralExpression) {
    return Pair(module, defaultExport)
  }
  return null
}

fun getContainingXmlFile(element: PsiElement): XmlFile? =
  (element.containingFile as? XmlFile
   ?: element as? XmlFile
   ?: InjectedLanguageManager.getInstance(
     element.project).getInjectionHost(element)?.containingFile as? XmlFile)
