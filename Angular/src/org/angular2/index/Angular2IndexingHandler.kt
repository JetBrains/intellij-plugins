// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.index

import com.intellij.lang.ASTNode
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.*
import com.intellij.lang.javascript.index.FrameworkIndexingHandler
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.*
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner
import com.intellij.lang.javascript.psi.impl.JSPropertyImpl
import com.intellij.lang.javascript.psi.stubs.*
import com.intellij.lang.javascript.psi.stubs.impl.JSElementIndexingDataImpl
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.lang.tree.util.children
import com.intellij.lang.typescript.TypeScriptStubElementTypes
import com.intellij.openapi.util.io.FileUtilRt.getNameWithoutExtension
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Consumer
import com.intellij.util.SmartList
import com.intellij.util.asSafely
import com.intellij.util.containers.ContainerUtil
import org.angular2.Angular2DecoratorUtil
import org.angular2.Angular2DecoratorUtil.ALIAS_PROP
import org.angular2.Angular2DecoratorUtil.ATTRIBUTE_DEC
import org.angular2.Angular2DecoratorUtil.COMPONENT_DEC
import org.angular2.Angular2DecoratorUtil.DIRECTIVE_DEC
import org.angular2.Angular2DecoratorUtil.FORWARD_REF_FUN
import org.angular2.Angular2DecoratorUtil.INJECT_FUN
import org.angular2.Angular2DecoratorUtil.INPUTS_PROP
import org.angular2.Angular2DecoratorUtil.INPUT_DEC
import org.angular2.Angular2DecoratorUtil.INPUT_FUN
import org.angular2.Angular2DecoratorUtil.MODEL_FUN
import org.angular2.Angular2DecoratorUtil.MODULE_DEC
import org.angular2.Angular2DecoratorUtil.NAME_PROP
import org.angular2.Angular2DecoratorUtil.OUTPUTS_PROP
import org.angular2.Angular2DecoratorUtil.OUTPUT_DEC
import org.angular2.Angular2DecoratorUtil.OUTPUT_FROM_OBSERVABLE_FUN
import org.angular2.Angular2DecoratorUtil.OUTPUT_FUN
import org.angular2.Angular2DecoratorUtil.PIPE_DEC
import org.angular2.Angular2DecoratorUtil.SELECTOR_PROP
import org.angular2.Angular2DecoratorUtil.STYLES_PROP
import org.angular2.Angular2DecoratorUtil.STYLE_URLS_PROP
import org.angular2.Angular2DecoratorUtil.STYLE_URL_PROP
import org.angular2.Angular2DecoratorUtil.TEMPLATE_PROP
import org.angular2.Angular2DecoratorUtil.TEMPLATE_URL_PROP
import org.angular2.Angular2DecoratorUtil.getClassForDecoratorElement
import org.angular2.Angular2DecoratorUtil.getProperty
import org.angular2.Angular2DecoratorUtil.getPropertyStringValue
import org.angular2.entities.Angular2EntityUtils
import org.angular2.entities.ivy.Angular2IvySymbolDef
import org.angular2.entities.source.Angular2SourceUtil.isStylesheet
import org.angular2.lang.Angular2Bundle
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.NonNls
import java.util.function.Predicate

class Angular2IndexingHandler : FrameworkIndexingHandler() {

  override fun shouldCreateStubForCallExpression(node: ASTNode): Boolean =
    isDecoratorCallStringArgStubbed(node)
    || (isDecoratorLikeFunctionCall(node) && isDirectiveField(node.treeParent))
    || getFunctionReferenceName(node)?.let {
      STUBBED_FUNCTIONS.contains(it) || (it == INJECT_FUN && isDirectiveField(node.treeParent))
    } == true

  override fun shouldCreateStubForLiteral(node: ASTNode): Boolean {
    val parent = node.treeParent ?: return false
    val parentPropName = getPropertyName(parent)
    val container = if (parentPropName == NAME_PROP || parentPropName == ALIAS_PROP) {
      parent.treeParent.takeIf { it.elementType === JSStubElementTypes.OBJECT_LITERAL_EXPRESSION }
        ?.treeParent
      ?: return false
    }
    else parent

    if (container.elementType === JSElementTypes.ARGUMENT_LIST) {
      val grandParent = container.treeParent
      return (grandParent != null
              && grandParent.elementType === JSStubElementTypes.CALL_EXPRESSION
              && shouldCreateStubForCallExpression(grandParent))
    }
    val property = if (container.elementType === JSElementTypes.ARRAY_LITERAL_EXPRESSION) {
      container.treeParent
    }
    else container
    val propName = getPropertyName(property)
    return propName != null && STUBBED_PROPERTIES.contains(propName)
  }

  override fun shouldCreateStubForArrayLiteral(node: ASTNode): Boolean {
    return node.treeParent?.treeParent
      ?.takeIf { it.elementType == TypeScriptStubElementTypes.TYPESCRIPT_VARIABLE }
      ?.psi?.asSafely<TypeScriptVariable>()
      ?.let { isStandalonePseudoModuleDeclaration(it) } == true
  }

  private fun getPropertyName(property: ASTNode): String? {
    if (property.elementType !== JSStubElementTypes.PROPERTY) return null
    val identifier = JSPropertyImpl.findNameIdentifier(property)
    return if (identifier != null) JSStringUtil.unquoteWithoutUnescapingStringLiteralValue(identifier.text) else null
  }

  override fun hasSignificantValue(expression: JSLiteralExpression): Boolean {
    return shouldCreateStubForLiteral(expression.node)
  }

  override fun processCallExpression(callExpression: JSCallExpression, outData: JSElementIndexingData) {
    if (isDecoratorLikeFunctionCall(callExpression.node) && isDirectiveField(callExpression.node.treeParent)) {
      createJSImplicitElementForDecoratorLikeFunctionCall(callExpression, outData)
    }
    else {
      val name = getFunctionReferenceName(callExpression.node)
      if (name != null && (
          STUBBED_FUNCTIONS.contains(name)
          || (name == INJECT_FUN && isDirectiveField(callExpression.node.treeParent)))
      ) {
        recordFunctionName(callExpression, outData, name)
      }
    }
  }

  override fun processDecorator(decorator: ES6Decorator, data: JSElementIndexingDataImpl?): JSElementIndexingDataImpl? {
    val enclosingClass = getClassForDecoratorElement(decorator)
                         ?: return data
    val decoratorName = decorator.decoratorName
    val isComponent = COMPONENT_DEC == decoratorName
    return when {
      PIPE_DEC == decoratorName -> {
        val notNullData = data ?: JSElementIndexingDataImpl()
        addPipe(enclosingClass, { notNullData.addImplicitElement(it) }, getPropertyStringValue(decorator, NAME_PROP))
        notNullData
      }
      isComponent || DIRECTIVE_DEC == decoratorName -> {
        val notNullData = data ?: JSElementIndexingDataImpl()
        val selector = getPropertyStringValue(decorator, SELECTOR_PROP)
        addDirective(enclosingClass, { notNullData.addImplicitElement(it) }, selector)
        if (isComponent) {
          addComponentExternalFilesRefs(decorator, "", { notNullData.addImplicitElement(it) },
                                        listOfNotNull(getTemplateFileUrl(decorator)))
          addComponentExternalFilesRefs(decorator, STYLESHEET_INDEX_PREFIX, { notNullData.addImplicitElement(it) },
                                        getStylesUrls(decorator))
        }
        notNullData
      }
      MODULE_DEC == decoratorName -> {
        val notNullData = data ?: JSElementIndexingDataImpl()
        addModule(enclosingClass) { notNullData.addImplicitElement(it) }
        notNullData
      }
      else -> data
    }
  }

  override fun indexImplicitElement(element: JSImplicitElementStructure, sink: IndexSink?): Boolean {
    if (sink == null) {
      return false
    }
    val userID = element.userString
    val index = if (userID != null) INDEX_MAP[userID] else null
    if (index == Angular2SourceDirectiveIndexKey) {
      var type = element.toImplicitElement(null).userStringData
      if (type != null && type.startsWith(DIRECTIVE_TYPE)) {
        type = type.substring(DIRECTIVE_TYPE.length)
        StringUtil.split(type, "/")
          .forEach { name -> sink.occurrence(index, name) }
      }
      return true
    }
    else if (index != null) {
      sink.occurrence(index, element.name)
      if (index == Angular2SourcePipeIndexKey) {
        sink.occurrence(Angular2SymbolIndex.KEY, element.name)
      }
      else {
        return true
      }
    }
    return false
  }

  override fun indexClassStub(jsClassStub: JSClassStub<*>, sink: IndexSink) {
    if (jsClassStub is TypeScriptClassStub) {
      val entityDef = Angular2IvySymbolDef.get(jsClassStub, false)
      if (entityDef != null) {
        if (entityDef is Angular2IvySymbolDef.Module) {
          sink.occurrence(Angular2IvyModuleIndexKey, NG_MODULE_INDEX_NAME)
        }
        else if (entityDef is Angular2IvySymbolDef.Pipe) {
          val name = entityDef.name
          if (name != null) {
            sink.occurrence(Angular2IvyPipeIndexKey, name)
            sink.occurrence(Angular2SymbolIndex.KEY, name)
          }
        }
        else if (entityDef is Angular2IvySymbolDef.Directive) {
          val selector = entityDef.selector
          if (selector != null) {
            for (indexName in Angular2EntityUtils.getDirectiveIndexNames(selector.trim { it <= ' ' })) {
              sink.occurrence(Angular2IvyDirectiveIndexKey, indexName)
            }
          }
        }
      }
    }
  }

  override fun getMarkers(elementToIndex: PsiElement): List<String> {
    if (elementToIndex is TypeScriptVariable
        && isStandalonePseudoModuleDeclaration(elementToIndex)) {
      return listOf(NG_PSEUDO_MODULE_DECLARATION_MARKER)
    }
    return emptyList()
  }

  private fun addComponentExternalFilesRefs(
    decorator: ES6Decorator,
    namePrefix: String,
    processor: Consumer<in JSImplicitElement>,
    fileUrls: List<String>,
  ) {
    for (fileUrl in fileUrls) {
      val lastSlash = fileUrl.lastIndexOf('/')
      val name = fileUrl.substring(lastSlash + 1)
      //don't index if file name matches TS file name and is in the same directory
      if ((lastSlash <= 0 || lastSlash == 1 && fileUrl[0] == '.')
          && getNameWithoutExtension(name) == getNameWithoutExtension(decorator.containingFile.originalFile.name)) {
        continue
      }
      val elementBuilder = JSImplicitElementImpl.Builder(namePrefix + name, decorator)
        .setUserString(this, ANGULAR2_TEMPLATE_URLS_INDEX_USER_STRING)
      processor.consume(elementBuilder.toImplicitElement())
    }
  }

  private fun addDirective(
    directiveClass: TypeScriptClass,
    processor: Consumer<in JSImplicitElement>,
    @NonNls selector: String?,
  ) {
    val indexNames: Set<String>
    if (selector == null) {
      indexNames = emptySet()
    }
    else {
      indexNames = Angular2EntityUtils.getDirectiveIndexNames(selector.trim { it <= ' ' })
    }
    val directive = JSImplicitElementImpl.Builder(directiveClass.name ?: selector ?: "<null>", directiveClass)
      .forbidAstAccess()
      .setType(JSImplicitElement.Type.Class)
      .setUserStringWithData(this, ANGULAR2_DIRECTIVE_INDEX_USER_STRING, DIRECTIVE_TYPE + StringUtil.join(indexNames, "/"))
      .toImplicitElement()
    processor.consume(directive)
  }

  private fun addPipe(
    pipeClass: TypeScriptClass,
    processor: Consumer<in JSImplicitElement>,
    @NonNls pipe: String?,
  ) {
    val pipeName = pipe ?: Angular2Bundle.message("angular.description.unnamed")
    val pipeElement = JSImplicitElementImpl.Builder(pipeName, pipeClass)
      .setUserStringWithData(this, ANGULAR2_PIPE_INDEX_USER_STRING, PIPE_TYPE)
      .setType(JSImplicitElement.Type.Class)
      .toImplicitElement()
    processor.consume(pipeElement)
  }

  private fun addModule(moduleClass: TypeScriptClass, processor: Consumer<JSImplicitElement>) {
    val pipeElement = JSImplicitElementImpl.Builder(NG_MODULE_INDEX_NAME, moduleClass)
      .setUserStringWithData(this, ANGULAR2_MODULE_INDEX_USER_STRING, MODULE_TYPE)
      .setType(JSImplicitElement.Type.Class)
      .toImplicitElement()
    processor.consume(pipeElement)
  }

  override fun computeJSImplicitElementUserStringKeys(): Set<String> {
    return INDEX_MAP.keys
  }

  private fun isDecoratorCallStringArgStubbed(callNode: ASTNode): Boolean {
    val parent = callNode.treeParent
    if (parent.elementType !== JSStubElementTypes.ES6_DECORATOR) return false
    val methodExpression = callNode.firstChildNode
    if (methodExpression.elementType !== JSElementTypes.REFERENCE_EXPRESSION) return false

    val referencedNameElement = methodExpression.firstChildNode.takeIf { it.elementType == JSTokenTypes.IDENTIFIER }
                                ?: return false
    val decoratorName = referencedNameElement.text
    return STUBBED_DECORATORS_STRING_ARGS.contains(decoratorName)
  }

  private fun isDecoratorLikeFunctionCall(callNode: ASTNode): Boolean {
    if (callNode.elementType !== JSStubElementTypes.CALL_EXPRESSION) return false

    val methodExpression = callNode.firstChildNode
    if (methodExpression.elementType !== JSElementTypes.REFERENCE_EXPRESSION) return false

    val referencedNameElement = methodExpression.firstChildNode
                                  // Recognize `input.required()` syntax
                                  .let { if (it.elementType == JSElementTypes.REFERENCE_EXPRESSION) it.firstChildNode else it }
                                  .takeIf { it.elementType == JSTokenTypes.IDENTIFIER }
                                ?: return false
    val decoratorLikeFunctionName = referencedNameElement.text
    return STUBBED_DECORATOR_LIKE_FUNCTIONS.contains(decoratorLikeFunctionName)
  }

  private fun getFunctionReferenceName(callNode: ASTNode): String? =
    callNode
      .takeIf { it.elementType === JSStubElementTypes.CALL_EXPRESSION }
      ?.firstChildNode
      ?.takeIf { it.elementType === JSElementTypes.REFERENCE_EXPRESSION }
      ?.firstChildNode
      ?.takeIf { it.elementType === JSTokenTypes.IDENTIFIER }
      ?.text

  private fun isDirectiveField(fieldNode: ASTNode?): Boolean {
    if (fieldNode?.elementType !== TypeScriptStubElementTypes.TYPESCRIPT_FIELD) return false
    val classNode = fieldNode?.treeParent?.treeParent
                      ?.takeIf { it.elementType === JSStubElementTypes.TYPESCRIPT_CLASS }
                    ?: return false
    return hasDecorator(classNode.psi as TypeScriptClass, COMPONENT_DEC, DIRECTIVE_DEC) != null
  }

  private fun createJSImplicitElementForDecoratorLikeFunctionCall(callExpression: JSCallExpression, outData: JSElementIndexingData) {
    val reference = callExpression.methodExpression as? JSReferenceExpression ?: return
    val referenceName = if (reference.hasQualifier()) {
      val qualifier = reference.qualifier as? JSReferenceExpression ?: return
      if (qualifier.hasQualifier())
        return
      else
        qualifier.referenceName + "." + reference.referenceName
    }
    else {
      reference.referenceName
    }
    recordFunctionName(callExpression, outData, referenceName ?: return)
  }

  private fun recordFunctionName(
    callExpression: JSCallExpression,
    outData: JSElementIndexingData,
    referenceName: String,
  ) {
    outData.addImplicitElement(
      JSImplicitElementImpl.Builder(referenceName, callExpression)
        .setUserStringWithData(
          this,
          ANGULAR2_FUNCTION_NAME_USER_STRING,
          null
        )
        .toImplicitElement()
    )
  }

  private fun hasDecorator(attributeListOwner: JSAttributeListOwner, vararg names: String): ES6Decorator? {
    val list = attributeListOwner.attributeList
    if (list == null || names.isEmpty()) {
      return null
    }
    for (decorator in PsiTreeUtil.getStubChildrenOfTypeAsList(list, ES6Decorator::class.java)) {
      if (names.contains(decorator.decoratorName)) {
        return decorator
      }
    }
    if (attributeListOwner is TypeScriptClassExpression) {
      val context = attributeListOwner.getContext() as? JSAttributeListOwner
      if (context != null) {
        return hasDecorator(context, *names)
      }
    }
    return null
  }

  private fun isStandalonePseudoModuleDeclaration(variable: TypeScriptVariable): Boolean {
    val attributeList = variable.attributeList
    if (attributeList == null
        || !variable.isConst
        || !attributeList.hasModifier(JSAttributeList.ModifierType.EXPORT)
        || variable is TypeScriptField)
      return false
    if (attributeList.hasModifier(JSAttributeList.ModifierType.DECLARE)) {
      // export declare const foo = readonly [typeof a, typeof b]
      // export declare const foo = [typeof a, typeof b]
      val tuple = variable.node.children().find { it.elementType == TypeScriptStubElementTypes.TUPLE_TYPE }
      return tuple != null && tuple.children()
        .filter { it.elementType == TypeScriptStubElementTypes.TUPLE_MEMBER_ELEMENT }
        .map { it.firstChildNode }
        .all {
          it?.elementType == TypeScriptStubElementTypes.TYPEOF_TYPE
          && it.lastChildNode?.elementType == JSElementTypes.REFERENCE_EXPRESSION
        }
    }
    else {
      // export const foo = [a,b] as const
      // export const foo = [a,b]
      val arr = variable.node.children()
                  .find { it.elementType == JSStubElementTypes.TYPE_AS_EXPRESSION }
                  ?.takeIf {
                    it.lastChildNode?.elementType == TypeScriptStubElementTypes.CONST_TYPE
                  }
                  ?.firstChildNode
                  ?.takeIf { it.elementType == JSElementTypes.ARRAY_LITERAL_EXPRESSION }
                ?: variable.node.children().find { it.elementType == JSElementTypes.ARRAY_LITERAL_EXPRESSION }
      return arr != null && arr.children().all {
        !JSExtendedLanguagesTokenSetProvider.EXPRESSIONS.contains(it.elementType)
        || it.elementType == JSElementTypes.REFERENCE_EXPRESSION
      }
    }
  }

  companion object {

    const val NG_MODULE_INDEX_NAME: String = "ngModule"

    const val NG_PSEUDO_MODULE_DECLARATION_MARKER: String = "a2pmd"
  }
}

private const val REQUIRE = "require"

private const val ANGULAR2_TEMPLATE_URLS_INDEX_USER_STRING = "a2tui"

private const val ANGULAR2_PIPE_INDEX_USER_STRING = "a2pi"

private const val ANGULAR2_DIRECTIVE_INDEX_USER_STRING = "a2di"

private const val ANGULAR2_MODULE_INDEX_USER_STRING = "a2mi"

private const val ANGULAR2_FUNCTION_NAME_USER_STRING = "a2fn"

private const val PIPE_TYPE = "P;;;"

private const val DIRECTIVE_TYPE = "D;;;"

private const val MODULE_TYPE = "M;;;"

private const val STYLESHEET_INDEX_PREFIX = "ss/"

val TS_CLASS_TOKENS: TokenSet = TokenSet.create(JSStubElementTypes.TYPESCRIPT_CLASS,
                                                JSStubElementTypes.TYPESCRIPT_CLASS_EXPRESSION)

private val STUBBED_PROPERTIES = setOf(
  TEMPLATE_URL_PROP, STYLE_URLS_PROP, STYLE_URL_PROP, SELECTOR_PROP, INPUTS_PROP, OUTPUTS_PROP)

private val STUBBED_DECORATORS_STRING_ARGS = setOf(
  INPUT_DEC, OUTPUT_DEC, ATTRIBUTE_DEC)

private val STUBBED_DECORATOR_LIKE_FUNCTIONS = setOf(
  INPUT_FUN, OUTPUT_FUN, OUTPUT_FROM_OBSERVABLE_FUN, MODEL_FUN)

private val STUBBED_FUNCTIONS = setOf(
  FORWARD_REF_FUN)

private val INDEX_MAP = mapOf<String, StubIndexKey<String, JSImplicitElementProvider>?>(
  ANGULAR2_TEMPLATE_URLS_INDEX_USER_STRING to Angular2TemplateUrlIndexKey,
  ANGULAR2_DIRECTIVE_INDEX_USER_STRING to Angular2SourceDirectiveIndexKey,
  ANGULAR2_PIPE_INDEX_USER_STRING to Angular2SourcePipeIndexKey,
  ANGULAR2_MODULE_INDEX_USER_STRING to Angular2SourceModuleIndexKey,
  ANGULAR2_FUNCTION_NAME_USER_STRING to null,
)

fun JSImplicitElement.isPipe(): Boolean {
  return ANGULAR2_PIPE_INDEX_USER_STRING == userString
}

fun JSImplicitElement.isDirective(): Boolean {
  return ANGULAR2_DIRECTIVE_INDEX_USER_STRING == userString
}

fun JSImplicitElement.isModule(): Boolean {
  return ANGULAR2_MODULE_INDEX_USER_STRING == userString
}

@ApiStatus.Internal
fun ES6Decorator.isStringArgStubbed(): Boolean {
  return STUBBED_DECORATORS_STRING_ARGS.contains(decoratorName)
}

@ApiStatus.Internal
fun isDecoratorLikeSignalFunction(name: String): Boolean {
  return STUBBED_DECORATOR_LIKE_FUNCTIONS.contains(name.takeWhile { it != '.' })
}

@ApiStatus.Internal
fun resolveComponentsFromIndex(file: PsiFile, filter: Predicate<ES6Decorator>): List<TypeScriptClass> {
  val name = (if (isStylesheet(file)) STYLESHEET_INDEX_PREFIX else "") + file.viewProvider.virtualFile.name
  val result = SmartList<TypeScriptClass>()
  Angular2IndexUtil.multiResolve(file.project, Angular2TemplateUrlIndexKey, name) { el ->
    if (el != null) {
      val componentDecorator = el.parent
      if (componentDecorator is ES6Decorator && filter.test(componentDecorator)) {
        ContainerUtil.addIfNotNull(result, getClassForDecoratorElement(componentDecorator))
      }
    }
    true
  }
  return result
}

fun getFunctionNameFromIndex(call: JSCallExpression): String? =
  call.indexingData
    ?.implicitElements
    ?.find { it.userString == ANGULAR2_FUNCTION_NAME_USER_STRING }
    ?.name

private fun getTemplateFileUrl(decorator: ES6Decorator): String? {
  val templateUrl = getPropertyStringValue(decorator, TEMPLATE_URL_PROP)
  if (templateUrl != null) {
    return templateUrl
  }
  val property = getProperty(decorator, TEMPLATE_PROP)
  return if (property != null) {
    getExprReferencedFileUrl(property.value)
  }
  else null
}

private fun getStylesUrls(decorator: ES6Decorator): List<String> {
  val result = SmartList<String>()

  val urlsGetter = { name: String, func: (JSExpression) -> String? ->
    getProperty(decorator, name)
      ?.value
      ?.asSafely<JSArrayLiteralExpression>()
      ?.expressions
      ?.mapNotNullTo(result, func)
  }

  urlsGetter(STYLE_URLS_PROP) { Angular2DecoratorUtil.getExpressionStringValue(it) }
  urlsGetter(STYLES_PROP) { getExprReferencedFileUrl(it) }
  getPropertyStringValue(decorator, STYLE_URL_PROP)?.let { result.add(it) }

  return result
}

@ApiStatus.Internal
fun getExprReferencedFileUrl(expression: JSExpression?): String? {
  if (expression is JSReferenceExpression) {
    for (resolvedElement in Angular2IndexUtil.resolveLocally(expression)) {
      if (resolvedElement is ES6ImportedBinding) {
        val from = resolvedElement.declaration?.fromClause
        if (from != null) {
          return from.referenceText?.let { StringUtil.unquoteString(it) }
        }
      }
    }
  }
  else if (expression is JSCallExpression) {
    val referenceExpression = expression.methodExpression as? JSReferenceExpression
    val arguments = expression.arguments
    if (arguments.size == 1
        && arguments[0] is JSLiteralExpression
        && (arguments[0] as JSLiteralExpression).isQuotedLiteral
        && referenceExpression != null
        && referenceExpression.qualifier == null
        && REQUIRE == referenceExpression.referenceName) {
      return (arguments[0] as JSLiteralExpression).stringValue
    }
  }
  return null
}
