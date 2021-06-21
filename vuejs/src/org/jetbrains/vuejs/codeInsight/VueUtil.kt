// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ecmascript6.psi.ES6ImportCall
import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.index.JSSymbolUtil
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptAsExpression
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils
import com.intellij.lang.javascript.psi.resolve.JSClassResolver
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.*
import com.intellij.lang.javascript.psi.types.evaluable.JSApplyNewType
import com.intellij.lang.javascript.psi.types.evaluable.JSReturnedExpressionType
import com.intellij.lang.javascript.psi.types.primitives.JSBooleanType
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil.isStubBased
import com.intellij.lang.typescript.modules.TypeScriptNodeReference
import com.intellij.lang.typescript.resolve.TypeScriptAugmentationUtil
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.impl.source.resolve.FileContextUtil
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider.Result.create
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.CachedValuesManager.getCachedValue
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ObjectUtils.tryCast
import com.intellij.util.castSafelyTo
import com.intellij.util.ui.EDT
import org.jetbrains.vuejs.index.findScriptTag
import org.jetbrains.vuejs.index.resolveLocally
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpression
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.VueModelProximityVisitor
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.source.PROPS_REQUIRED_PROP
import org.jetbrains.vuejs.model.source.PROPS_TYPE_PROP
import org.jetbrains.vuejs.types.asCompleteType
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

const val LANG_ATTRIBUTE_NAME = "lang"
const val SETUP_ATTRIBUTE_NAME = "setup"
const val REF_ATTRIBUTE_NAME = "ref"
const val MODULE_ATTRIBUTE_NAME = "module"
const val ATTR_DIRECTIVE_PREFIX = "v-"
const val ATTR_EVENT_SHORTHAND = '@'
const val ATTR_SLOT_SHORTHAND = '#'
const val ATTR_ARGUMENT_PREFIX = ':'
const val ATTR_MODIFIER_PREFIX = '.'

val VUE_NOTIFICATIONS: NotificationGroup
  get() = NotificationGroupManager.getInstance().getNotificationGroup("Vue")

fun fromAsset(name: String, hyphenBeforeDigit: Boolean = false): String {
  val result = StringBuilder()
  for (ch in name) {
    when {
      ch.isUpperCase() -> {
        if (result.isNotEmpty()) {
          result.append('-')
        }
        result.append(StringUtil.toLowerCase(ch))
      }
      ch in '0'..'9' -> {
        if (hyphenBeforeDigit) {
          result.append('-')
        }
        result.append(ch)
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
fun getTextIfLiteral(holder: PsiElement?): String? =
  (if (holder is JSReferenceExpression) {
    resolveLocally(holder).mapNotNull { (it as? JSVariable)?.initializerOrStub }.firstOrNull()
  }
  else holder)
    ?.castSafelyTo<JSLiteralExpression>()
    ?.let { literalExpr ->
      when {
        (literalExpr as? StubBasedPsiElement<*>)?.stub != null -> literalExpr.significantValue?.let { es6Unquote(it) }
        literalExpr.isQuotedLiteral -> literalExpr.stringValue
        else -> null
      }
    }

fun detectLanguage(tag: XmlTag?): String? = tag?.getAttribute(LANG_ATTRIBUTE_NAME)?.value?.trim()

fun detectVueScriptLanguage(file: PsiFile): String? {
  val xmlFile = file as? XmlFile ?: return null
  val scriptTag = findScriptTag(xmlFile) ?: return null
  return detectLanguage(scriptTag)
}

val BOOLEAN_TYPE get() = JSBooleanType(true, JSTypeSource.EXPLICITLY_DECLARED, JSTypeContext.INSTANCE)

fun objectLiteralFor(element: PsiElement?): JSObjectLiteralExpression? {
  return resolveElementTo(element, JSObjectLiteralExpression::class) as? JSObjectLiteralExpression?
}

fun resolveElementTo(element: PsiElement?, vararg classes: KClass<out JSElement>): JSElement? {
  val queue = ArrayDeque<PsiElement>()
  queue.add(element ?: return null)
  val visited = HashSet<PsiElement>()
  loop@ while (!queue.isEmpty()) {
    val cur = queue.removeFirst()
    if (visited.add(cur)) {
      if (classes.any { it.isInstance(cur) }) return cur as? JSElement
      when (cur) {
        is JSFunction -> {
          JSStubBasedPsiTreeUtil.findReturnedExpressions(cur).asSequence()
            .filter { JSReturnedExpressionType.isCountableReturnedExpression(it) || it is ES6ImportCall }
            .toCollection(queue)
        }
        is JSInitializerOwner -> {
          ( // Try with stub
            when (cur) {
              is JSProperty -> cur.objectLiteralExpressionInitializer ?: cur.tryGetFunctionInitializer()
              is JSVariable -> cur.initializerOrStub
              else -> null
            }
            // Try extract reference name from type
            ?: JSPsiImplUtils.getInitializerReference(cur)?.let { JSStubBasedPsiTreeUtil.resolveLocally(it, cur) }
            // Most expensive solution through substitution, works with function calls
            ?: (element as? JSTypeOwner)?.jsType?.substitute()?.sourceElement
          )?.let { queue.addLast(it) }
        }
        is PsiPolyVariantReference -> cur.multiResolve(false)
          .mapNotNullTo(queue) { if (it.isValidResult) it.element else null }
        is ES6ImportCall -> cur.resolveReferencedElements()
          .toCollection(queue)
        is JSEmbeddedContent -> findDefaultExport(cur)?.let { queue.add(it) }
        else -> JSStubBasedPsiTreeUtil.calculateMeaningfulElements(cur)
          .toCollection(queue)
      }
    }
  }
  return null
}

fun collectPropertiesRecursively(element: JSObjectLiteralExpression): List<Pair<String, JSProperty>> {
  val result = mutableListOf<Pair<String, JSProperty>>()
  val initialPropsList = element.propertiesIncludingSpreads
  val queue = ArrayDeque<JSElement>(initialPropsList.size)
  queue.addAll(initialPropsList)
  while (queue.isNotEmpty()) {
    when (val property = queue.pollLast()) {
      is JSSpreadExpression -> {
        objectLiteralFor(property.expression)
          ?.propertiesIncludingSpreads
          ?.toCollection(queue)
      }
      is JSProperty -> {
        if (property.name != null) {
          result.add(Pair(property.name!!, property))
        }
      }
    }
  }
  return result
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

fun getJSTypeFromPropOptions(expression: JSExpression?): JSType? =
  when (expression) {
    is JSArrayLiteralExpression -> JSCompositeTypeImpl.getCommonType(
      expression.expressions.map { getJSTypeFromConstructor(it) },
      JSTypeSource.EXPLICITLY_DECLARED, false
    )
    is JSObjectLiteralExpression -> expression.findProperty(PROPS_TYPE_PROP)
      ?.value
      ?.let {
        when (it) {
          is JSArrayLiteralExpression -> getJSTypeFromPropOptions(it)
          else -> getJSTypeFromConstructor(it)
        }
      }
    null -> null
    else -> getJSTypeFromConstructor(expression)
  }

private fun getJSTypeFromConstructor(expression: JSExpression): JSType =
  (expression as? TypeScriptAsExpression)
    ?.type?.jsType?.castSafelyTo<JSGenericTypeImpl>()
    ?.takeIf { (it.type as? JSTypeImpl)?.typeText == "PropType" }
    ?.arguments?.getOrNull(0)
    ?.asCompleteType()
  ?: JSApplyNewType(JSTypeofTypeImpl(expression, JSTypeSourceFactory.createTypeSource(expression, false)),
                    JSTypeSourceFactory.createTypeSource(expression.containingFile, false))

fun getRequiredFromPropOptions(expression: JSExpression?): Boolean =
  (expression as? JSObjectLiteralExpression)
    ?.findProperty(PROPS_REQUIRED_PROP)
    ?.literalExpressionInitializer
    ?.let {
      it.isBooleanLiteral && "true" == it.significantValue
    }
  ?: false

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

fun getHostFile(context: PsiElement): PsiFile? {
  val original = CompletionUtil.getOriginalOrSelf(context)
  val hostFile = FileContextUtil.getContextFile(if (original !== context) original else context.containingFile.originalFile)
  return hostFile?.originalFile
}

fun findDefaultExport(element: PsiElement?): PsiElement? =
  element?.let {
    (ES6PsiUtil.findDefaultExport(element) as? JSExportAssignment)?.stubSafeElement
    ?: findDefaultCommonJSExport(it)
  }

private fun findDefaultCommonJSExport(element: PsiElement): PsiElement? {
  return JSClassResolver.getInstance().findElementsByQNameIncludingImplicit(JSSymbolUtil.MODULE_EXPORTS, element.containingFile)
    .asSequence()
    .filterIsInstance<JSDefinitionExpression>()
    .mapNotNull { it.initializerOrStub }
    .firstOrNull()
}

private val resolveSymbolCache = ConcurrentHashMap<String, Key<CachedValue<*>>>()

fun <T : PsiElement> resolveSymbolFromNodeModule(scope: PsiElement?, moduleName: String, symbolName: String, symbolClass: Class<T>): T? {
  @Suppress("UNCHECKED_CAST")
  val key: Key<CachedValue<T>> = resolveSymbolCache.computeIfAbsent("$moduleName/$symbolName/${symbolClass.simpleName}") {
    Key.create(it)
  } as Key<CachedValue<T>>
  val file = scope?.containingFile ?: return null
  return getCachedValue(file, key) {
    TypeScriptNodeReference(file, moduleName, 0).resolve()
      ?.castSafelyTo<JSElement>()
      ?.let { module ->
        val symbols = ES6PsiUtil.resolveSymbolInModule(symbolName, file, module)
        if (symbols.isEmpty()) {
          TypeScriptAugmentationUtil.getModuleAugmentations(module, module)
            .asSequence()
            .filterIsInstance<JSElement>()
            .flatMap { ES6PsiUtil.resolveSymbolInModule(symbolName, file, it).asSequence() }
        }
        else {
          symbols.asSequence()
        }
      }
      ?.filter { it.element?.isValid == true }
      ?.mapNotNull { tryCast(it.element, symbolClass) }
      ?.firstOrNull()
      ?.let {
        return@getCachedValue create(it, PsiModificationTracker.MODIFICATION_COUNT)
      }
    create(null, PsiModificationTracker.MODIFICATION_COUNT)
  }
}

fun resolveLocalComponent(context: VueEntitiesContainer, tagName: String, containingFile: PsiFile): List<VueComponent> {
  val result = mutableListOf<VueComponent>()
  val normalizedTagName = fromAsset(tagName)
  context.acceptEntities(object : VueModelProximityVisitor() {
    override fun visitComponent(name: String, component: VueComponent, proximity: Proximity): Boolean {
      return acceptSameProximity(proximity, fromAsset(name) == normalizedTagName) {
        // Cannot self refer without export declaration with component name
        if ((component.source as? JSImplicitElement)?.context != containingFile) {
          result.add(component)
        }
      }
    }
  }, VueModelVisitor.Proximity.GLOBAL)
  return result
}