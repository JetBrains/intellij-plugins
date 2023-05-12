// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ecmascript6.psi.ES6ImportCall
import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.index.JSSymbolUtil
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptAsExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptVariable
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils
import com.intellij.lang.javascript.psi.resolve.JSClassResolver
import com.intellij.lang.javascript.psi.resolve.QualifiedItemProcessor
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.*
import com.intellij.lang.javascript.psi.types.evaluable.JSApplyNewType
import com.intellij.lang.javascript.psi.types.evaluable.JSReturnedExpressionType
import com.intellij.lang.javascript.psi.types.primitives.JSBooleanType
import com.intellij.lang.javascript.psi.types.primitives.JSPrimitiveType
import com.intellij.lang.javascript.psi.types.primitives.JSUndefinedType
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.lang.javascript.psi.util.stubSafeStringValue
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.impl.source.resolve.FileContextUtil
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.asSafely
import com.intellij.util.text.SemVer
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_HTML
import com.intellij.webSymbols.utils.unwrapMatchedSymbols
import org.jetbrains.vuejs.index.VUE_FILE_EXTENSION
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.index.findScriptTag
import org.jetbrains.vuejs.index.resolveLocally
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpressionContent
import org.jetbrains.vuejs.lang.html.VueFile
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.VueModelProximityVisitor
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.source.MODEL_LOCAL_PROP
import org.jetbrains.vuejs.model.source.PROPS_DEFAULT_PROP
import org.jetbrains.vuejs.model.source.PROPS_REQUIRED_PROP
import org.jetbrains.vuejs.model.source.PROPS_TYPE_PROP
import org.jetbrains.vuejs.types.asCompleteType
import org.jetbrains.vuejs.web.VueWebSymbolsQueryConfigurator
import java.util.*
import kotlin.reflect.KClass

const val LANG_ATTRIBUTE_NAME = "lang"
const val SETUP_ATTRIBUTE_NAME = "setup"
const val REF_ATTRIBUTE_NAME = "ref"
const val MODULE_ATTRIBUTE_NAME = "module"
const val GENERIC_ATTRIBUTE_NAME = "generic"
const val ATTR_DIRECTIVE_PREFIX = "v-"
const val ATTR_EVENT_SHORTHAND = '@'
const val ATTR_SLOT_SHORTHAND = '#'
const val ATTR_ARGUMENT_PREFIX = ':'
const val ATTR_MODIFIER_PREFIX = '.'

const val VITE_PKG = "vite"

val VUE_NOTIFICATIONS: NotificationGroup
  get() = NotificationGroupManager.getInstance().getNotificationGroup("Vue")

fun fromAsset(name: String, hyphenBeforeDigit: Boolean = false): String {
  val result = StringBuilder()
  for (ch in name) {
    when {
      ch.isUpperCase() -> {
        if (result.isNotEmpty()
            && result.last() != '-') {
          result.append('-')
        }
        result.append(StringUtil.toLowerCase(ch))
      }
      ch in '0'..'9' -> {
        if (hyphenBeforeDigit
            && result.isNotEmpty()
            && result.last() != '-') {
          result.append('-')
        }
        result.append(ch)
      }
      else -> result.append(ch)
    }
  }
  return result.toString()
}

fun toAsset(name: String, capitalized: Boolean = false): String {
  val result = StringBuilder()
  var nextCapitalized = capitalized
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

fun JSPsiNamedElementBase.resolveIfImportSpecifier(): JSPsiNamedElementBase =
  (this as? ES6ImportSpecifier)
    ?.multiResolve(false)
    ?.asSequence()
    ?.mapNotNull { it.takeIf { it.isValidResult }?.element as? JSPsiNamedElementBase }
    ?.firstOrNull()
  ?: this

private val QUOTES = setOf('\'', '"', '`')

fun es6Unquote(s: String): String {
  if (s.length < 2) return s
  if (QUOTES.contains(s[0]) && s.endsWith(s[0])) return s.substring(1, s.length - 1)
  return s
}

fun getStringLiteralsFromInitializerArray(holder: PsiElement): List<JSLiteralExpression> {
  return JSStubBasedPsiTreeUtil.findDescendants<JSLiteralExpression>(
    holder, TokenSet.create(JSStubElementTypes.LITERAL_EXPRESSION,
                            JSStubElementTypes.STRING_TEMPLATE_EXPRESSION))
    .filter {
      val context = it.context
      !it.significantValue.isNullOrBlank() &&
      QUOTES.contains(it.significantValue!![0]) &&
      ((context is JSArrayLiteralExpression) && (context.context == holder) || context == holder)
    }
}

fun getTextIfLiteral(holder: PsiElement?, forceStubs: Boolean = true): String? =
  (if (holder is JSReferenceExpression) {
    resolveLocally(holder).firstNotNullOfOrNull { (it as? JSVariable)?.initializerOrStub }
  }
  else holder)
    ?.asSafely<JSLiteralExpression>()
    ?.let { literalExpr ->
      when {
        forceStubs -> literalExpr.stubSafeStringValue
        literalExpr.isQuotedLiteral -> literalExpr.stringValue
        else -> null
      }
    }

fun detectLanguage(tag: XmlTag?): String? = tag?.getAttribute(LANG_ATTRIBUTE_NAME)?.value?.trim()

fun detectVueScriptLanguage(file: PsiFile): String? {
  val xmlFile = file as? XmlFile ?: return null
  val scriptTag = findScriptTag(xmlFile, false) ?: findScriptTag(xmlFile, true) ?: return null
  return detectLanguage(scriptTag)
}

fun objectLiteralFor(element: PsiElement?): JSObjectLiteralExpression? {
  return resolveElementTo(element, JSObjectLiteralExpression::class)
}

fun <T : PsiElement> resolveElementTo(element: PsiElement?, vararg classes: KClass<out T>): T? {
  val queue = ArrayDeque<PsiElement>()
  queue.add(element ?: return null)
  val visited = HashSet<PsiElement>()
  loop@ while (!queue.isEmpty()) {
    val cur = queue.removeFirst()
    if (visited.add(cur)) {
      if (cur !is JSEmbeddedContent && cur !is JSVariable && classes.any { it.isInstance(cur) }) {
        @Suppress("UNCHECKED_CAST")
        return cur as T
      }
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
              is TypeScriptVariable -> cur.initializerOrStub ?: run {
                // Typed components from d.ts.
                if (cur.typeElement != null && classes.any { it.isInstance(cur) })
                  @Suppress("UNCHECKED_CAST")
                  return cur as T
                else null
              }
              is JSVariable -> cur.initializerOrStub
              else -> null
            }
            // Try extract reference name from type
            ?: JSPsiImplUtils.getInitializerReference(cur)?.let { JSStubBasedPsiTreeUtil.resolveLocally(it, cur) }
            // Most expensive solution through substitution, works with function calls
            ?: (cur as? JSTypeOwner)?.jsType?.substitute()?.sourceElement
          )?.let { queue.addLast(it) }
        }
        is PsiPolyVariantReference -> cur.multiResolve(false)
          .mapNotNullTo(queue) { if (it.isValidResult) it.element else null }
        is ES6ImportCall -> cur.resolveReferencedElements()
          .toCollection(queue)
        is JSEmbeddedContent -> {
          if (cur.context.let { tag ->
              tag is XmlTag && PsiTreeUtil.getStubChildrenOfTypeAsList(tag, XmlAttribute::class.java)
                .find { it.name == SETUP_ATTRIBUTE_NAME } != null
            }) {
            val regularScript = findModule(cur, false)
            if (regularScript != null) {
              queue.add(regularScript)
            }
            else if (classes.any { it == JSEmbeddedContent::class }) {
              @Suppress("UNCHECKED_CAST")
              return cur as T
            }
            else return null
          }
          else findDefaultExport(cur)?.let { queue.add(it) }
        }
        else -> JSStubBasedPsiTreeUtil.calculateMeaningfulElements(cur)
          .toCollection(queue)
      }
    }
  }
  return null
}

fun collectMembers(element: JSObjectLiteralExpression): List<Pair<String, JSElement>> {
  val result = mutableListOf<Pair<String, JSElement>>()
  val initialPropsList = element.propertiesIncludingSpreads
  val queue = ArrayDeque<JSElement>(initialPropsList.size)
  queue.addAll(initialPropsList)
  val visited = mutableSetOf<PsiElement>()
  while (queue.isNotEmpty()) {
    val property = queue.pollLast()
    if (!visited.add(property)) continue
    when (property) {
      is JSSpreadExpression -> {
        processJSTypeMembers(property.innerExpressionType).toCollection(result)
      }
      is JSProperty -> {
        if (property.name != null) {
          result.add(Pair(property.name!!, property))
        }
      }
      else -> processJSTypeMembers(JSTypeUtils.getTypeOfElement(element)).toCollection(result)
    }
  }
  return result
}

fun processJSTypeMembers(type: JSType?): List<Pair<String, JSElement>> =
  type?.asRecordType()
    ?.properties
    ?.filter { it.hasValidName() }
    ?.flatMap { prop ->
      QualifiedItemProcessor
        .getElementsForTypeMember(prop, null, false)
        .filterIsInstance<JSElement>()
        .map { Pair(prop.memberName, it) }
    }
  ?: emptyList()

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

fun JSType.fixPrimitiveTypes(): JSType =
  transformTypeHierarchy {
    if (it is JSPrimitiveType && !it.isPrimitive)
      JSNamedTypeFactory.createType(it.primitiveTypeText, it.source, it.typeContext)
    else it
  }

private fun getJSTypeFromConstructor(expression: JSExpression): JSType {
  return getPropTypeFromGenericType((expression as? TypeScriptAsExpression)?.type?.jsType)
         ?: JSApplyNewType(JSTypeofTypeImpl(expression, JSTypeSourceFactory.createTypeSource(expression, false)),
                           JSTypeSourceFactory.createTypeSource(expression.containingFile, false))
}

private val PROPS_CONTAINER_TYPES = setOf("PropType", "PropOptions")

fun getPropTypeFromGenericType(jsType: JSType?): JSType? =
  jsType
    ?.asSafely<JSGenericTypeImpl>()
    ?.takeIf { (it.type as? JSTypeImpl)?.typeText in PROPS_CONTAINER_TYPES }
    ?.arguments?.getOrNull(0)
    ?.asCompleteType()

fun getRequiredFromPropOptions(expression: JSExpression?): Boolean =
  getBooleanFromPropOptions(expression, PROPS_REQUIRED_PROP)

fun getLocalFromPropOptions(expression: JSExpression?): Boolean =
  getBooleanFromPropOptions(expression, MODEL_LOCAL_PROP)

private fun getBooleanFromPropOptions(expression: JSExpression?, propName: String) =
  (expression as? JSObjectLiteralExpression)
    ?.findProperty(propName)
    ?.jsType
    ?.let { type ->
      if (type is JSWidenType) type.originalType else type
    }
    ?.let { type ->
      (type as? JSBooleanLiteralTypeImpl)?.literal
    }
  ?: false

fun getPropOptionality(options: JSExpression?, required: Boolean): Boolean =
  when (val defaultType = getDefaultTypeFromPropOptions(options)) {
    null -> if (required) false else getJSTypeFromPropOptions(options)?.substitute() !is JSBooleanType
    is JSUndefinedType -> true
    is JSFunctionType -> defaultType.returnType?.substitute() is JSUndefinedType
    else -> false
  }

fun getDefaultTypeFromPropOptions(expression: JSExpression?): JSType? =
  (expression as? JSObjectLiteralExpression)
    ?.findProperty(PROPS_DEFAULT_PROP)
    ?.jsType
    ?.substitute()

inline fun <reified T : JSExpression> XmlAttributeValue.findJSExpression(): T? {
  return findVueJSEmbeddedExpressionContent()?.firstChild as? T
}

fun XmlAttributeValue.findVueJSEmbeddedExpressionContent(): VueJSEmbeddedExpressionContent? {
  val root = when {
    language === VueLanguage.INSTANCE ->
      children.find { it is ASTWrapperPsiElement }
    textLength >= 2 ->
      InjectedLanguageManager.getInstance(project)
        .findInjectedElementAt(containingFile, textOffset + 1)
        ?.containingFile
    else -> null
  }
  return root?.firstChild?.asSafely<VueJSEmbeddedExpressionContent>()
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

fun SemVer.withoutPreRelease() =
  if (this.preRelease != null)
    SemVer("${this.major}.${this.minor}.${this.patch}", this.major, this.minor, this.patch)
  else this

fun WebSymbol.extractComponentSymbol(): WebSymbol? =
  this.takeIf { it.namespace == NAMESPACE_HTML }
    ?.unwrapMatchedSymbols()
    ?.toList()
    ?.takeIf { it.size == 2 && it[0].pattern != null }
    ?.get(1)
    ?.takeIf { it.kind == VueWebSymbolsQueryConfigurator.KIND_VUE_COMPONENTS }

inline fun <reified T : PsiElement> PsiElement.parentOfTypeInAttribute(): T? {
  val host = InjectedLanguageManager.getInstance(project).getInjectionHost(this) ?: this
  return host.parentOfType<T>()
}

fun isScriptSetupLocalDirectiveName(name: String): Boolean =
  name.length > 1 && name[0] == 'v' && name[1].isUpperCase()

fun createVueFileFromText(project: Project, text: String): VueFile =
  PsiFileFactory.getInstance(project)
    .createFileFromText("dummy$VUE_FILE_EXTENSION", VueFileType.INSTANCE, text) as VueFile