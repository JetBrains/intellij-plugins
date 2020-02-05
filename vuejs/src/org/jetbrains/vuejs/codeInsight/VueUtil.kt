// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils
import com.intellij.lang.javascript.psi.types.*
import com.intellij.lang.javascript.psi.types.primitives.JSBooleanType
import com.intellij.lang.javascript.psi.types.primitives.JSNumberType
import com.intellij.lang.javascript.psi.types.primitives.JSStringType
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil.isStubBased
import com.intellij.lang.typescript.modules.TypeScriptNodeReference
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.impl.source.resolve.FileContextUtil
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider.Result.create
import com.intellij.psi.util.CachedValuesManager.getCachedValue
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ObjectUtils.tryCast
import com.intellij.util.castSafelyTo
import one.util.streamex.StreamEx
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.index.findScriptTag
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpression
import org.jetbrains.vuejs.lang.html.VueLanguage
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashSet

const val LANG_ATTRIBUTE_NAME = "lang"
const val ATTR_DIRECTIVE_PREFIX = "v-"
const val ATTR_EVENT_SHORTHAND = '@'
const val ATTR_SLOT_SHORTHAND = '#'
const val ATTR_ARGUMENT_PREFIX = ':'
const val ATTR_MODIFIER_PREFIX = '.'

fun fromAsset(name: String): String {
  // TODO ensure that this conversion conforms to Vue.js rules
  val result = StringBuilder()
  for (ch in name) {
    when {
      ch.isUpperCase() -> {
        if (result.isNotEmpty()) {
          result.append('-')
        }
        result.append(StringUtil.toLowerCase(ch))
      }
      else -> result.append(ch)
    }
  }
  return result.toString()
}

fun toAsset(name: String): String {
  val result = StringBuilder()
  var nextCapitalized = false
  for (ch in name) {
    when {
      ch == '-' -> nextCapitalized = true
      nextCapitalized -> {
        result.append(StringUtil.toUpperCase(ch))
        nextCapitalized = false
      }
      else -> result.append(ch)
    }
  }
  return result.toString()
}

private val QUOTES = setOf('\'', '"', '`')
fun es6Unquote(s: String): String {
  if (s.length < 2) return s
  if (QUOTES.contains(s[0]) && s.endsWith(s[0])) return s.substring(1, s.length - 1)
  return s
}

fun getStringLiteralsFromInitializerArray(holder: PsiElement): List<JSLiteralExpression> {
  return JSStubBasedPsiTreeUtil.findDescendants<JSLiteralExpression>(holder,
                                                                     TokenSet.create(JSStubElementTypes.LITERAL_EXPRESSION,
                                                                                     JSStubElementTypes.STRING_TEMPLATE_EXPRESSION))
    .filter {
      val context = it.context
      !it.significantValue.isNullOrBlank() &&
      QUOTES.contains(it.significantValue!![0]) &&
      ((context is JSArrayLiteralExpression) && (context.parent == holder) || context == holder)
    }
}

@StubSafe
fun getTextIfLiteral(holder: PsiElement?): String? {
  if (holder != null && holder is JSLiteralExpression) {
    if ((holder as? StubBasedPsiElement<*>)?.stub != null) {
      return holder.significantValue?.let { es6Unquote(it) }
    }
    if (holder.isQuotedLiteral) {
      return holder.stringValue
    }
  }
  return null
}

fun detectLanguage(tag: XmlTag?): String? = tag?.getAttribute(LANG_ATTRIBUTE_NAME)?.value?.trim()

fun detectVueScriptLanguage(file: PsiFile): String? {
  val xmlFile = file as? XmlFile ?: return null
  val scriptTag = findScriptTag(xmlFile) ?: return null
  return detectLanguage(scriptTag)
}

val BOOLEAN_TYPE = JSBooleanType(true, JSTypeSource.EXPLICITLY_DECLARED, JSTypeContext.INSTANCE)

private val vueTypesMap = mapOf(
  Pair("Boolean", BOOLEAN_TYPE),
  Pair("String", JSStringType(true, JSTypeSource.EXPLICITLY_DECLARED, JSTypeContext.INSTANCE)),
  Pair("Number", JSNumberType(true, JSTypeSource.EXPLICITLY_DECLARED, JSTypeContext.INSTANCE)),
  Pair("Function", JSFunctionTypeImpl(JSTypeSource.EXPLICITLY_DECLARED, listOf(), null)),
  Pair("Array", JSArrayTypeImpl(null, JSTypeSource.EXPLICITLY_DECLARED))
)

fun objectLiteralFor(element: PsiElement?): JSObjectLiteralExpression? {
  val queue = ArrayDeque<PsiElement>()
  queue.add(element ?: return null)
  val visited = HashSet<PsiElement>()
  loop@ while (!queue.isEmpty()) {
    val cur = queue.removeFirst()
    if (visited.add(cur)) {
      when (cur) {
        is JSObjectLiteralExpression -> return cur
        is JSInitializerOwner -> {
          when (cur) {
            is JSProperty -> cur.objectLiteralExpressionInitializer?.let { return it }
            is JSVariable -> {
              val initializer = cur.initializerOrStub
              if (initializer != null) {
                queue.addLast(initializer)
                continue@loop
              }
            }
          }
          JSPsiImplUtils.getInitializerReference(cur)
            ?.let { JSStubBasedPsiTreeUtil.resolveLocally(it, cur) }
            ?.let { queue.addLast(it) }
        }
        is JSVariable -> cur.initializerOrStub?.let { queue.addLast(it) }
        is PsiPolyVariantReference -> cur.multiResolve(false)
          .mapTo(queue) { if (it.isValidResult) it.element else null }
        else -> JSStubBasedPsiTreeUtil.calculateMeaningfulElements(cur)
          .toCollection(queue)
      }
    }
  }
  return null
}

fun getStubSafeCallArguments(call: JSCallExpression): List<PsiElement> {
  if (isStubBased(call)) {
    (call as StubBasedPsiElementBase<*>).stub?.let { stub ->
      val methodExpr = call.stubSafeMethodExpression
      return stub.childrenStubs.map { it.psi }
        .filter { it !== methodExpr }
        .toList()
    }
    return call.arguments.filter { isStubBased(it) }.toList()
  }
  return emptyList()
}

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
  return reference.referenceName?.let { vueTypesMap[it] }
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
    ?.firstOrNull()
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

fun getHostFile(context: PsiElement): PsiFile? {
  val original = CompletionUtil.getOriginalOrSelf(context)
  val hostFile = FileContextUtil.getContextFile(if (original !== context) original else context.containingFile.originalFile)
  return hostFile?.originalFile
}

private val resolveSymbolCache = ConcurrentHashMap<String, Key<CachedValue<*>>>()

fun <T : PsiElement> resolveSymbolFromNodeModule(scope: PsiElement?, moduleName: String, symbolName: String, symbolClass: Class<T>): T? {
  @Suppress("UNCHECKED_CAST")
  val key: Key<CachedValue<T>> = resolveSymbolCache.computeIfAbsent("$moduleName/$symbolName/${symbolClass.simpleName}") {
    Key.create(it)
  } as Key<CachedValue<T>>
  return getCachedValue(scope ?: return null, key) {
    TypeScriptNodeReference(scope, moduleName, 0).resolve()
      ?.castSafelyTo<JSElement>()
      ?.let { ES6PsiUtil.resolveSymbolInModule(symbolName, scope, it) }
      ?.asSequence()
      ?.filter { it.element?.isValid == true }
      ?.mapNotNull { tryCast(it.element, symbolClass) }
      ?.firstOrNull()
      ?.let {
        return@getCachedValue create(it, PsiModificationTracker.MODIFICATION_COUNT)
      }
    create<T>(null, PsiModificationTracker.MODIFICATION_COUNT)
  }
}
