// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.index

import com.intellij.lang.ASTNode
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.ecmascript6.psi.ES6FunctionProperty
import com.intellij.lang.ecmascript6.psi.JSClassExpression
import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil
import com.intellij.lang.javascript.*
import com.intellij.lang.javascript.index.FrameworkIndexingHandler
import com.intellij.lang.javascript.index.JSSymbolUtil
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator
import com.intellij.lang.javascript.psi.resolve.JSTypeInfo
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData
import com.intellij.lang.javascript.psi.stubs.JSImplicitElementStructure
import com.intellij.lang.javascript.psi.stubs.impl.JSElementIndexingDataImpl
import com.intellij.lang.javascript.psi.types.*
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.impl.source.PsiFileImpl
import com.intellij.psi.impl.source.tree.TreeUtil
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil
import org.jetbrains.vuejs.codeInsight.VueFrameworkInsideScriptSpecificHandlersFactory
import org.jetbrains.vuejs.codeInsight.completion.vuex.VueStoreUtils
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.codeInsight.toAsset
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.expr.VueVForExpression
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.model.source.VueComponents
import org.jetbrains.vuejs.model.source.VueComponents.Companion.isComponentDecorator

class VueFrameworkHandler : FrameworkIndexingHandler() {
  // 1 here we are just mapping the constants, no lifecycle needed
  // 2 not lazy structure -> no synchronization needed
  // what to do, indexing is done on background thread, component initialization on EDT
  // either we synchronize (but then every access would have penalty)
  // or we are using the static structure
  // here there are only 6 indexes...
  private val VUE_INDEXES = mapOf(
    record(VueComponentsIndex.KEY),
    record(VueExtendsBindingIndex.KEY),
    record(VueGlobalDirectivesIndex.KEY),
    record(VueLocalDirectivesIndex.KEY),
    record(VueMixinBindingIndex.KEY),
    record(VueOptionsIndex.KEY),
    record(VueStoreIndex.KEY)
  )
  private val expectedLiteralOwnerExpressions = TokenSet.create(JSStubElementTypes.CALL_EXPRESSION,
                                                                JSStubElementTypes.NEW_EXPRESSION,
                                                                JSStubElementTypes.ASSIGNMENT_EXPRESSION)


  companion object {
    private fun record(key: StubIndexKey<String, JSImplicitElementProvider>): Pair<String, StubIndexKey<String, JSImplicitElementProvider>> {
      return Pair(VueIndexBase.createJSKey(key), key)
    }

    const val VUE: String = "Vue"
    private const val METHOD = "methods"
    private const val COMPUTED = "computed"
    private const val DATA = "data"
    private const val PROPS = "props"
    private const val VUE_INSTANCE = "CombinedVueInstance"

    private val VUE_DESCRIPTOR_OWNERS = arrayOf(VUE, "mixin", "component", "extends", "directive", "delimiters")
    private val COMPONENT_INDICATOR_PROPS = setOf("template", "render", "mixins", "components", "props")
    fun hasComponentIndicatorProperties(obj: JSObjectLiteralExpression): Boolean =
      obj.properties.any { COMPONENT_INDICATOR_PROPS.contains(it.name) }

    fun isDefaultExports(expression: JSExpression?): Boolean =
      expression is JSReferenceExpression && JSSymbolUtil.isAccurateReferenceExpressionName(expression as JSReferenceExpression?,
                                                                                            JSSymbolUtil.EXPORTS, "default")

  }

  override fun findModule(result: PsiElement): PsiElement? = org.jetbrains.vuejs.index.findModule(result)

  override fun addContextType(info: JSTypeInfo, context: PsiElement) {
    if (context.containingFile.fileType != VueFileType.INSTANCE) return
    if (!VueFrameworkInsideScriptSpecificHandlersFactory.isInsideScript(context)) return
    val parent = PsiTreeUtil.findFirstParent(context, Condition {
      return@Condition it is JSObjectLiteralExpression && it.parent is ES6ExportDefaultAssignment
    })
    if (parent == null) return
    if (context !is JSReferenceExpression) return
    if (context.qualifier !is JSThisExpression) return
    if (context.parent.parent is ES6ExportDefaultAssignment) return
    val processGenericType = processGenericType(context, parent)
    if (processGenericType != null) info.addRecordType(processGenericType)
  }

  private fun processGenericType(context: JSReferenceExpression,
                                 parent: PsiElement?): JSRecordType? {
    val typeAlias = JSFileReferencesUtil.resolveModuleReference(context.containingFile, "vue/types/vue").firstOrNull()
    if (typeAlias == null) return null
    val typeSource = JSTypeSourceFactory.createTypeSource(typeAlias, false)
    val vueType = JSNamedTypeFactory.createType(VUE_INSTANCE, typeSource, JSContext.INSTANCE)
    val vue = JSNamedTypeFactory.createType(VUE, typeSource, JSContext.INSTANCE)
    val methodPropertyType = JSResolveUtil.getElementJSType((parent as JSObjectLiteralExpression).findProperty(METHOD))
    val computedPropertyType = JSResolveUtil.getElementJSType(parent.findProperty(COMPUTED))
    val propsPropertyType = JSResolveUtil.getElementJSType(parent.findProperty(PROPS))
    val dataFunction = (parent.findProperty(DATA) as? ES6FunctionProperty)
    val dataStream = JSTypeUtils.getFunctionType(
      JSResolveUtil.getElementJSType(dataFunction),
      false,
      context)
      .filter { it is JSFunctionType }.findFirst()
    val dataPropertyType = if (dataStream.isPresent) (dataStream.get() as? JSFunctionType)?.returnType
    else null ?: JSTypeCastUtil.NO_RECORD_TYPE
    val genericArguments = listOf(vue, getContextualType(dataPropertyType), getContextualType(methodPropertyType),
                                  getContextualType(computedPropertyType), getContextualType(propsPropertyType))
    return JSGenericTypeImpl(typeSource, vueType, genericArguments).asRecordType()
  }

  private fun getContextualType(type: JSType?): JSType {
    if (type == null || type is JSAnyType) return JSTypeCastUtil.NO_RECORD_TYPE
    if (type !is JSCompositeTypeImpl) return type
    return JSContextualUnionTypeImpl.getContextualUnionType(type.types, type.source)
  }

  override fun processAnyProperty(property: JSProperty, outData: JSElementIndexingData?): JSElementIndexingData? {
    val obj = property.parent as JSObjectLiteralExpression

    val out = outData ?: JSElementIndexingDataImpl()
    //Bootstrap-vue components
    if (property.containingFile.name == "index.js" && property.parent is JSObjectLiteralExpression) {
      val parent = PsiTreeUtil.findFirstParent(property, Condition {
        return@Condition it is JSVarStatement && it.variables.firstOrNull()?.name == "components"
      })
      if (parent != null) {
        val componentName = property.name ?: ""
        out.addImplicitElement(createImplicitElement(componentName, property, VueComponentsIndex.JS_KEY))
      }
    }
    if (MIXINS_PROP == property.name && property.value is JSArrayLiteralExpression) {
      (property.value as JSArrayLiteralExpression).expressions
        .forEach {
          if (it is JSReferenceExpression) {
            recordMixin(out, property, it, false)
          }
          else if (it is JSObjectLiteralExpression && it.firstProperty != null) {
            recordMixin(out, it.firstProperty!!, null, false)
          }
        }
    }
    else if (EXTENDS_PROP == property.name && property.value is JSReferenceExpression) {
      recordExtends(out, property, property.value, false)
    }
    else if (DIRECTIVES_PROP == property.name) {
      (property.value as? JSObjectLiteralExpression)?.properties?.forEach { directive ->
        if (!directive.name.isNullOrBlank()) {
          if (directive.value is JSReferenceExpression) {
            recordDirective(out, directive, directive.name!!, directive.value, false)
          }
          else if (directive.value is JSObjectLiteralExpression || directive.value is JSFunction) {
            recordDirective(out, directive, directive.name!!, null, false)
          }
        }
      }
    }
    //Vuetify typescript components
    else if (NAME_PROP == property.name && property.value is JSLiteralExpression) {
      val componentName = (property.value as JSLiteralExpression).stringValue
      if (componentName != null && obj.containingFile.name.contains(toAsset(componentName),
                                                                    true) && obj.containingFile.fileType is TypeScriptFileType) {
        out.addImplicitElement(createImplicitElement(componentName, property, VueComponentsIndex.JS_KEY))
      }
    }
    else if (VueStoreUtils.STATE == property.name) {
      val properties = PsiTreeUtil.findChildrenOfType(property, JSProperty::class.java)
      properties
        .filter { it.parent.parent == property }
        .forEach {
          if (it.name != null) {
            out.addImplicitElement(createImplicitElement(it.name!!, it, VueStoreIndex.JS_KEY, VueStoreUtils.STATE))
          }
        }
    }
    else if (VueStoreUtils.ACTION == property.name || VueStoreUtils.MUTATION == property.name || VueStoreUtils.GETTER == property.name) {
      //Actions can be action: function(){} or action(){}
      val es6properties = PsiTreeUtil.findChildrenOfType(property, ES6FunctionProperty::class.java)
      val jsProperties = PsiTreeUtil.findChildrenOfType(property, JSProperty::class.java)

      es6properties
        .filter { it.parent.parent == property }
        .forEach {
          //          For such cases:
          //          var SOME_MUTATION = 'computed name'
          //          mutations = {
          //            [SOME_MUTATION]() {
          //            }
          //          };
          if (it.computedPropertyName != null) {
            val expr = PsiTreeUtil.findChildOfType(it, JSReferenceExpression::class.java)
            if (expr != null && expr.referenceName != null) {
              val reference = JSSymbolUtil.resolveLocallyIncludingDefinitions(expr.referenceName!!, expr)
              val referenceText = PsiTreeUtil.findChildOfType(reference, JSLiteralExpression::class.java)?.value
              if (referenceText != null) out.addImplicitElement(
                createImplicitElement(referenceText.toString(), it, VueStoreIndex.JS_KEY, property.name))
            }
          }
          if (it.name != null) {
            out.addImplicitElement(createImplicitElement(it.name!!, it, VueStoreIndex.JS_KEY, property.name))
          }

        }
      jsProperties
        .filter { it.parent.parent == property }
        .forEach {
          if (it.name != null) {
            out.addImplicitElement(createImplicitElement(it.name!!, it, VueStoreIndex.JS_KEY, property.name))
          }
        }
    }

    val firstProperty = obj.firstProperty ?: return outData
    if (firstProperty == property) {
      val parent = obj.parent
      if (parent is JSExportAssignment ||
          (parent is JSAssignmentExpression && isDefaultExports(parent.definitionExpression?.expression))) {
        if (obj.containingFile.fileType == VueFileType.INSTANCE || obj.containingFile is JSFile && hasComponentIndicatorProperties(obj)) {
          out.addImplicitElement(createImplicitElement(getComponentNameFromDescriptor(obj), property, VueComponentsIndex.JS_KEY))
        }
      }
      else if (((parent as? JSProperty) == null) && isDescriptorOfLinkedInstanceDefinition(obj)) {
        val binding = (obj.findProperty("el")?.value as? JSLiteralExpression)?.stringValue
        out.addImplicitElement(createImplicitElement(binding ?: "", property, VueOptionsIndex.JS_KEY))
      }
    }
    return if (out.isEmpty) outData else out
  }

  override fun processDecorator(decorator: ES6Decorator, data: JSElementIndexingDataImpl?): JSElementIndexingDataImpl? {
    if (!isComponentDecorator(decorator)) return data

    val exportAssignment = (decorator.parent as? JSAttributeList)?.parent as? ES6ExportDefaultAssignment ?: return data
    val classExpression = exportAssignment.stubSafeElement as? JSClassExpression<*> ?: return data

    val nameProperty = VueComponents.getDescriptorFromDecorator(decorator)?.findProperty("name")
    val name = getTextIfLiteral(nameProperty?.value) ?: FileUtil.getNameWithoutExtension(decorator.containingFile.name)
    val outData = data ?: JSElementIndexingDataImpl()
    outData.addImplicitElement(createImplicitElement(name, classExpression, VueComponentsIndex.JS_KEY, null, null, false))
    return outData
  }

  override fun shouldCreateStubForCallExpression(node: ASTNode?): Boolean {
    val reference = (node?.psi as? JSCallExpression)?.methodExpression as? JSReferenceExpression ?: return false
    return VueStaticMethod.matchesAny(reference)
  }

  override fun processCallExpression(callExpression: JSCallExpression?, outData: JSElementIndexingData) {
    val reference = callExpression?.methodExpression as? JSReferenceExpression ?: return
    val arguments = callExpression.arguments
    if (arguments.isEmpty()) return

    if (VueStaticMethod.Component.matches(reference)) {
      if (arguments.size >= 2) {
        var componentName = getTextIfLiteral(arguments[0])
        var nameRefString: String? = null
        if (componentName == null) {
          val nameRef = arguments[0] as? JSReferenceExpression ?: return
          nameRefString = nameRef.text
          val qualifierRef = nameRef.qualifier as? JSReferenceExpression
          componentName = (qualifierRef?.referenceName ?: nameRef.referenceName) + GLOBAL_BINDING_MARK
        }
        val provider: PsiElement = (arguments[1] as? JSObjectLiteralExpression)?.firstProperty ?: callExpression
        outData.addImplicitElement(createImplicitElement(componentName, provider, VueComponentsIndex.JS_KEY,
                                                         nameRefString, arguments[1], true))
      }
    }
    else if (VueStaticMethod.Mixin.matches(reference)) {
      if (arguments.size == 1) {
        val provider = (arguments[0] as? JSObjectLiteralExpression)?.firstProperty ?: callExpression
        recordMixin(outData, provider, arguments[0], true)
      }
    }
    else if (VueStaticMethod.Directive.matches(reference)) {
      val directiveName = getTextIfLiteral(arguments[0])
      if (arguments.size >= 2 && directiveName != null && !directiveName.isNullOrBlank()) {
        recordDirective(outData, callExpression, directiveName, arguments[1], true)
      }
    }
  }

  private fun recordDirective(outData: JSElementIndexingData,
                              provider: JSImplicitElementProvider,
                              directiveName: String,
                              descriptorRef: PsiElement?,
                              isGlobal: Boolean) {
    val index = if (isGlobal) VueGlobalDirectivesIndex.JS_KEY else VueLocalDirectivesIndex.JS_KEY
    outData.addImplicitElement(createImplicitElement(directiveName, provider, index, null, descriptorRef, isGlobal))
  }

  private fun recordMixin(outData: JSElementIndexingData,
                          provider: JSImplicitElementProvider,
                          descriptorRef: PsiElement?,
                          isGlobal: Boolean) {
    outData.addImplicitElement(createImplicitElement(if (isGlobal) GLOBAL else LOCAL, provider, VueMixinBindingIndex.JS_KEY, null,
                                                     descriptorRef, isGlobal))
  }

  private fun recordExtends(outData: JSElementIndexingData,
                            provider: JSImplicitElementProvider,
                            descriptorRef: PsiElement?,
                            isGlobal: Boolean) {
    outData.addImplicitElement(createImplicitElement(if (isGlobal) GLOBAL else LOCAL, provider, VueExtendsBindingIndex.JS_KEY, null,
                                                     descriptorRef, isGlobal))
  }

  private fun isDescriptorOfLinkedInstanceDefinition(obj: JSObjectLiteralExpression): Boolean {
    val argumentList = obj.parent as? JSArgumentList ?: return false
    if (argumentList.arguments[0] == obj) {
      return JSSymbolUtil.isAccurateReferenceExpressionName(
        (argumentList.parent as? JSNewExpression)?.methodExpression as? JSReferenceExpression, VUE) ||
             JSSymbolUtil.isAccurateReferenceExpressionName(
               (argumentList.parent as? JSCallExpression)?.methodExpression as? JSReferenceExpression, VUE, "extends")
    }
    return false
  }

  override fun shouldCreateStubForLiteral(node: ASTNode?): Boolean {
    if (node?.psi is JSLiteralExpression) {
      return hasSignificantValue(node.psi as JSLiteralExpression)
    }
    return super.shouldCreateStubForLiteral(node)
  }

  override fun hasSignificantValue(expression: JSLiteralExpression): Boolean {
    val parentType = expression.node.treeParent?.elementType ?: return false
    if (JSElementTypes.ARRAY_LITERAL_EXPRESSION == parentType ||
        JSElementTypes.PROPERTY == parentType && "required" == expression.node.treeParent.findChildByType(JSTokenTypes.IDENTIFIER)?.text) {
      return VueFileType.INSTANCE == expression.containingFile.fileType || insideVueDescriptor(expression)
    }
    return false
  }

  // limit building stub in other file types like js/html to Vue-descriptor-like members
  private fun insideVueDescriptor(expression: JSLiteralExpression): Boolean {
    val statement = TreeUtil.findParent(expression.node,
                                        expectedLiteralOwnerExpressions,
                                        JSExtendedLanguagesTokenSetProvider.STATEMENTS) ?: return false
    val referenceHolder = if (statement.elementType == JSStubElementTypes.ASSIGNMENT_EXPRESSION)
      statement.findChildByType(JSStubElementTypes.DEFINITION_EXPRESSION)
    else statement
    val ref = referenceHolder?.findChildByType(JSElementTypes.REFERENCE_EXPRESSION) ?: return false
    return ref.getChildren(JSKeywordSets.IDENTIFIER_NAMES).filter { it.text in VUE_DESCRIPTOR_OWNERS }.any()
  }

  private fun getComponentNameFromDescriptor(obj: JSObjectLiteralExpression): String {
    return ((obj.findProperty("name")?.value as? JSLiteralExpression)?.stringValue
            ?: FileUtil.getNameWithoutExtension(obj.containingFile.name))
  }

  override fun indexImplicitElement(element: JSImplicitElementStructure, sink: IndexSink?): Boolean {
    val index = VUE_INDEXES[element.userString]
    if (index != null) {
      sink?.occurrence(index, element.name)
    }
    return false
  }

  override fun addTypeFromResolveResult(evaluator: JSTypeEvaluator,
                                        context: JSEvaluateContext,
                                        result: PsiElement): Boolean {
    if (!isVueContext(result)) return false
    if (result is JSVariable && result.language is VueJSLanguage) {
      val vFor = result.parent as? VueVForExpression ?: result.parent.parent as? VueVForExpression
      val vForRef = vFor?.getReferenceExpression()
      val variables = vFor?.getVarStatement()?.variables
      if (vForRef != null && variables != null && variables.isNotEmpty() && result == variables[0]) {
        if (JSPsiImplUtils.calculateTypeOfVariableForIteratedExpression(evaluator, vForRef, vFor)) return true
      }
    }
    return false
  }
}

fun findModule(element: PsiElement?): JSEmbeddedContent? {
  val file = element as? XmlFile ?: element?.containingFile as? XmlFile
  if (file != null && file.fileType == VueFileType.INSTANCE) {
    if (file is PsiFileImpl) {
      val greenStub = file.greenStub
      //stub-safe path
      if (greenStub != null) {
        val children = greenStub.getChildrenByType<JSElement>(JSExtendedLanguagesTokenSetProvider.MODULE_EMBEDDED_CONTENTS,
                                                              JSEmbeddedContent.ARRAY_FACTORY)
        val result = children.firstOrNull()
        return if (result is JSEmbeddedContent) result else null
      }
    }
    val script = findScriptTag(file)
    if (script != null) {
      return PsiTreeUtil.findChildOfType(script, JSEmbeddedContent::class.java)
    }
  }
  return null
}

fun findScriptTag(xmlFile: XmlFile): XmlTag? {
  if (xmlFile.fileType == VueFileType.INSTANCE) {
    val visitor = MyScriptVisitor()
    xmlFile.accept(visitor)
    return visitor.scriptTag
  }
  return null
}

private enum class VueStaticMethod(val methodName: String) {
  Component("component"), Mixin("mixin"), Directive("directive");

  companion object {
    fun matchesAny(reference: JSReferenceExpression): Boolean = values().any { it.matches(reference) }
  }

  fun matches(reference: JSReferenceExpression): Boolean =
    JSSymbolUtil.isAccurateReferenceExpressionName(reference, VueFrameworkHandler.VUE, methodName)
}

private class MyScriptVisitor : VueFileVisitor() {
  internal var jsElement: JSEmbeddedContent? = null
  internal var scriptTag: XmlTag? = null

  override fun visitXmlTag(tag: XmlTag?) {
    if (HtmlUtil.isScriptTag(tag)) {
      scriptTag = tag
      jsElement = PsiTreeUtil.findChildOfType(tag, JSEmbeddedContent::class.java)
    }
  }
}

open class VueFileVisitor : XmlElementVisitor() {
  override fun visitXmlDocument(document: XmlDocument?): Unit = recursion(document)

  override fun visitXmlFile(file: XmlFile?): Unit = recursion(file)

  protected fun recursion(element: PsiElement?) {
    element?.children?.forEach { it.accept(this) }
  }
}
