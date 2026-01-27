// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.lang.ecmascript6.psi.ES6ImportCall
import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.documentation.JSDocumentationUtils
import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider.withTypeEvaluationLocation
import com.intellij.lang.javascript.index.JSSymbolUtil
import com.intellij.lang.javascript.modules.NodeModuleUtil
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.JSComputedPropertyNameOwner
import com.intellij.lang.javascript.psi.ecma6.TypeScriptAsExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptPropertySignature
import com.intellij.lang.javascript.psi.ecma6.TypeScriptVariable
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils
import com.intellij.lang.javascript.psi.resolve.JSClassResolver
import com.intellij.lang.javascript.psi.resolve.QualifiedItemProcessor
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
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.text.StringUtil
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.html.NAMESPACE_HTML
import com.intellij.polySymbols.query.PolySymbolWithPattern
import com.intellij.polySymbols.utils.namespace
import com.intellij.polySymbols.utils.unwrapMatchedSymbols
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.FileContextUtil
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.asSafely
import com.intellij.util.text.SemVer
import com.intellij.xml.util.HtmlUtil.LANG_ATTRIBUTE_NAME
import org.jetbrains.vuejs.index.VUE_FILE_EXTENSION
import org.jetbrains.vuejs.index.findScriptTag
import org.jetbrains.vuejs.index.resolveLocally
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpressionContent
import org.jetbrains.vuejs.lang.html.VueFile
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.jetbrains.vuejs.lang.html.psi.impl.VueScriptSetupEmbeddedContentImpl
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.source.*
import org.jetbrains.vuejs.types.asCompleteType
import org.jetbrains.vuejs.web.symbols.VueWebTypesMergedSymbol
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KClass

val VUE_NOTIFICATIONS: NotificationGroup
  get() = NotificationGroupManager.getInstance().getNotificationGroup("Vue")

/**
 * Convert to hyphen-case.
 */
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
            && result.last().let { it != '-' && !it.isDigit() }) {
          result.append('-')
        }
        result.append(ch)
      }
      else -> result.append(ch)
    }
  }
  return result.toString()
}

/**
 * Convert to camel-case.
 */
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

fun getStringLiteralsFromInitializerArray(holder: PsiElement): List<JSLiteralExpression> {
  return JSStubBasedPsiTreeUtil.findDescendants<JSLiteralExpression>(
    holder, TokenSet.create(JSElementTypes.LITERAL_EXPRESSION,
                            JSElementTypes.STRING_TEMPLATE_EXPRESSION))
    .filter { expr ->
      val context = expr.context
      expr.significantValue.let {
        !it.isNullOrBlank()
        && "'\"`".indexOf(it[0]) >= 0
      }
      && ((context is JSArrayLiteralExpression) && (context.context == holder) || context == holder)
    }
}

@OptIn(ExperimentalContracts::class)
fun getTextLiteralExpression(holder: PsiElement?, forceStubs: Boolean = true): JSLiteralExpression? {
  contract { returnsNotNull() implies (holder != null) }
  return (if (holder is JSReferenceExpression) {
    resolveLocally(holder).firstNotNullOfOrNull { (it as? JSVariable)?.initializerOrStub }
  }
  else holder)
    ?.asSafely<JSLiteralExpression>()
    ?.takeIf {
      (forceStubs && !it.stubSafeStringValue.isNullOrBlank())
      || (!forceStubs && it.isQuotedLiteral && !it.stringValue.isNullOrBlank())
    }
}

fun getTextIfLiteral(holder: PsiElement?, forceStubs: Boolean = true): String? =
  getTextLiteralExpression(holder, forceStubs)
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
  val scriptTag = findScriptTag(xmlFile, setup = true)
                  ?: findScriptTag(xmlFile, setup = false)
                  ?: return null
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
              is TypeScriptPropertySignature -> JSStubBasedPsiTreeUtil.calculateMeaningfulElement(cur).takeIf { it != cur }
              else -> null
            }
            ?: withTypeEvaluationLocation(element) {
              // Try extract reference name from type
              JSPsiImplUtils.getInitializerReference(cur)?.let { JSStubBasedPsiTreeUtil.resolveLocally(it, cur) }
              // Most expensive solution through substitution, works with function calls
              ?: getFromType(cur)
            }
          )?.let { queue.addLast(it) }
        }
        is PsiPolyVariantReference -> cur.multiResolve(false)
          .mapNotNullTo(queue) { if (it.isValidResult) it.element else null }
        is ES6ImportCall -> cur.resolveReferencedElements()
          .toCollection(queue)

        is VueScriptSetupEmbeddedContentImpl -> {
          val exportScope = cur.contextExportScope

          if (classes.any { it == JSEmbeddedContent::class }
              && (exportScope == null || findDefaultExport(exportScope) == null)) {
            @Suppress("UNCHECKED_CAST")
            return cur as T
          }

          exportScope ?: return null

          queue.add(exportScope)
        }

        is JSEmbeddedContent,
          -> findDefaultExport(cur)?.let { queue.add(it) }

        else -> JSStubBasedPsiTreeUtil.calculateMeaningfulElements(cur)
          .toCollection(queue)
      }
    }
  }
  return null
}

private fun getFromType(cur: PsiElement?): PsiElement? {
  val jsType = (cur as? JSTypeOwner)?.jsType
                 // Functional components do not have source - save on substitution time
                 ?.takeIf { it !is JSGenericTypeImpl || it.type.typeText != FUNCTIONAL_COMPONENT_TYPE }
                 ?.substitute() ?: return null
  val sourceElement = jsType.sourceElement
  return when {
    jsType is JSFunctionType -> sourceElement
    jsType is JSFreshObjectLiteralType -> sourceElement
    sourceElement is ES6ImportCall -> sourceElement
    else -> null
  }
}

fun collectMembers(element: JSObjectLiteralExpression, includeComputed: Boolean = false): List<Pair<String, JSElement>> {
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
        else if (includeComputed && property is JSComputedPropertyNameOwner) {
          property.computedPropertyName?.expressionAsReferenceName?.let {
            result.add(Pair(it, property))
          }
        }
      }
      else -> processJSTypeMembers(JSTypeUtils.getTypeOfElement(element)).toCollection(result)
    }
  }
  return result
}

fun processJSTypeMembers(type: JSType?): List<Pair<String, JSElement>> {
  val jsRecordType = type?.asRecordType()
  val isSimpleRecordWithDefinedSource = jsRecordType is JSSimpleRecordTypeImpl && jsRecordType.sourceElement != null
  return jsRecordType
           ?.properties
           ?.filter { it.hasValidName() }
           ?.flatMap { prop ->
             QualifiedItemProcessor
               .getElementsForTypeMember(prop, null, !isSimpleRecordWithDefinedSource)
               .filterIsInstance<JSElement>()
               .map { Pair(prop.memberName, it) }
           }
         ?: emptyList()
}

fun getPropTypeFromPropOptions(expression: JSExpression?): JSType? =
  when (expression) {
    is JSArrayLiteralExpression -> JSCompositeTypeFactory.getCommonType(
      expression.expressions.map { getPropTypeFromConstructor(it) },
      JSTypeSource.EXPLICITLY_DECLARED, false
    )
    is JSObjectLiteralExpression -> expression.findProperty(PROPS_TYPE_PROP)
      ?.let { typeProp ->
        getPropTypeFromDocComment(typeProp)
        ?: typeProp.value?.let { typeExpr ->
          when (typeExpr) {
            is JSArrayLiteralExpression -> getPropTypeFromPropOptions(typeExpr)
            else -> getPropTypeFromConstructor(typeExpr)
          }
        }
      }
    null -> null
    else -> getPropTypeFromConstructor(expression)
  }

fun getPropTypeFromDocComment(element: PsiNamedElement): JSType? =
  JSDocumentationUtils.findType(element as? JSNamedElement)
    ?.let { JSTypeParser.createTypeFromJSDoc(element.getProject(), it, JSTypeSourceFactory.createTypeSource(element, true)) }
    ?.let { getPropTypeFromGenericType(it) ?: it }

fun JSType.fixPrimitiveTypes(): JSType =
  transformTypeHierarchy {
    if (it is JSPrimitiveType && !it.isPrimitive)
      JSNamedTypeFactory.createType(it.primitiveTypeText, it.source, it.typeContext)
    else if (it is JSTypeImpl && JSCommonTypeNames.WRAPPER_TO_PRIMITIVE_TYPE_MAP.containsKey(it.typeText)) {
      JSNamedTypeFactory.createType(JSCommonTypeNames.WRAPPER_TO_PRIMITIVE_TYPE_MAP[it.typeText]!!, it.source, it.typeContext)
    }
    else it
  }

private fun getPropTypeFromConstructor(expression: JSExpression): JSType {
  return getPropTypeFromGenericType((expression as? TypeScriptAsExpression)?.type?.jsType)
         ?: JSApplyNewType(JSTypeofTypeImpl(expression, JSTypeSourceFactory.createTypeSource(expression, false)),
                           JSTypeSourceFactory.createTypeSource(expression.containingFile, false))
}

fun getPropTypeFromGenericType(jsType: JSType?): JSType? {
  val propType = when (jsType) {
    is JSRecordType -> jsType.findPropertySignature(PROPS_TYPE_PROP)?.jsType
    else -> jsType
  }

  return propType
    ?.asSafely<JSGenericTypeImpl>()
    ?.takeIf {
      when (val innerType = it.type) {
        is JSImportType -> innerType.qualifiedName.name
        is JSTypeImpl -> innerType.typeText
        else -> null
      } in PROPS_CONTAINER_TYPES
    }
    ?.arguments?.getOrNull(0)
    ?.asCompleteType()
}

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
    null -> if (required) false else getPropTypeFromPropOptions(options)?.substitute()?.fixPrimitiveTypes() !is JSBooleanType
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
    language === VueLanguage ->
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
    override fun visitComponent(component: VueNamedComponent, proximity: Proximity): Boolean {
      return acceptSameProximity(proximity, fromAsset(component.name) == normalizedTagName) {
        // Cannot self refer without export declaration with component name
        if (component !is VueFileComponent || component.source != containingFile) {
          result.add(component)
        }
      }
    }
  }, VueModelVisitor.Proximity.GLOBAL)
  return result
}

fun SemVer.withoutPreRelease(): SemVer =
  if (this.preRelease != null)
    SemVer("${this.major}.${this.minor}.${this.patch}", this.major, this.minor, this.patch)
  else this

fun PolySymbol.extractComponentSymbol(): VueNamedComponent? =
  this as? VueNamedComponent
  ?: (this as? VueWebTypesMergedSymbol)
    ?.delegate
    ?.asSafely<VueNamedComponent>()
  ?: this.takeIf { it.namespace == NAMESPACE_HTML }
    ?.unwrapMatchedSymbols()
    ?.toList()
    ?.takeIf { it.size == 2 && it[0] is PolySymbolWithPattern }
    ?.get(1)
    ?.let {
      if (it is VueWebTypesMergedSymbol) it.delegate else it
    }
    ?.asSafely<VueNamedComponent>()

inline fun <reified T : PsiElement> PsiElement.parentOfTypeInAttribute(): T? {
  val host = InjectedLanguageManager.getInstance(project).getInjectionHost(this) ?: this
  return host.parentOfType<T>()
}

fun isGlobalDirectiveName(name: String): Boolean =
  name.length > 1 && name[0] == 'v' && name[1].isUpperCase()

fun isScriptSetupLocalDirectiveName(name: String): Boolean =
  name.length > 1 && name[0] == 'v' && name[1].isUpperCase()

fun createVueFileFromText(project: Project, text: String): VueFile =
  PsiFileFactory.getInstance(project)
    .createFileFromText("dummy$VUE_FILE_EXTENSION", VueFileType, text) as VueFile

@NlsSafe
fun getLibraryNameForDocumentationOf(element: PsiElement?): String? =
  element
    ?.containingFile
    ?.virtualFile
    ?.let { PackageJsonUtil.findUpPackageJson(it) }
    ?.takeIf { NodeModuleUtil.hasNodeModulesDirInPath(it, null) }
    ?.let { PackageJsonData.getOrCreate(it) }
    ?.takeIf { it.name != null }
    ?.let { data ->
      data.name + (data.version?.toString()?.let { "@$it" } ?: "")
    }