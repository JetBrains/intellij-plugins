// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.index

import com.intellij.lang.ASTNode
import com.intellij.lang.ecmascript6.ES6StubElementTypes
import com.intellij.lang.ecmascript6.psi.*
import com.intellij.lang.javascript.*
import com.intellij.lang.javascript.index.FrameworkIndexingHandler
import com.intellij.lang.javascript.index.JSSymbolUtil
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData
import com.intellij.lang.javascript.psi.stubs.JSImplicitElementStructure
import com.intellij.lang.javascript.psi.stubs.impl.JSElementIndexingDataImpl
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.impl.source.PsiFileImpl
import com.intellij.psi.impl.source.tree.TreeUtil
import com.intellij.psi.impl.source.xml.stub.XmlTagStub
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.PathUtil
import com.intellij.util.castSafelyTo
import com.intellij.xml.util.HtmlUtil.SCRIPT_TAG_NAME
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.codeInsight.toAsset
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.model.source.*
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
    record(VueMixinBindingIndex.KEY),
    record(VueOptionsIndex.KEY),
    record(VueUrlIndex.KEY),
    record(VueIdIndex.KEY),
    record(VueGlobalFiltersIndex.KEY)
  )
  private val expectedLiteralOwnerExpressions = TokenSet.create(JSStubElementTypes.CALL_EXPRESSION,
                                                                JSStubElementTypes.NEW_EXPRESSION,
                                                                JSStubElementTypes.ASSIGNMENT_EXPRESSION,
                                                                ES6StubElementTypes.EXPORT_DEFAULT_ASSIGNMENT)


  companion object {
    fun <T : PsiElement> record(key: StubIndexKey<String, T>): Pair<String, StubIndexKey<String, T>> {
      return Pair(VueIndexBase.createJSKey(key), key)
    }

    private const val REQUIRE = "require"
    private const val VUE_INSTANCE = "CombinedVueInstance"

    private val VUE_DESCRIPTOR_OWNERS = arrayOf(VUE_NAMESPACE, MIXIN_FUN, COMPONENT_FUN, EXTEND_FUN, DIRECTIVE_FUN, DELIMITERS_PROP,
                                                FILTER_FUN, DEFINE_COMPONENT_FUN)
    private val COMPONENT_INDICATOR_PROPS = setOf(TEMPLATE_PROP, DATA_PROP, "render", PROPS_PROP, "propsData", COMPUTED_PROP, METHODS_PROP,
                                                  "watch", MIXINS_PROP, COMPONENTS_PROP, DIRECTIVES_PROP, FILTERS_PROP, SETUP_METHOD,
                                                  MODEL_PROP)

    private val INTERESTING_PROPERTIES = arrayOf(MIXINS_PROP, EXTENDS_PROP, DIRECTIVES_PROP, NAME_PROP, TEMPLATE_PROP)

    fun hasComponentIndicatorProperties(obj: JSObjectLiteralExpression, exclude: String? = null): Boolean =
      obj.properties.any { it.name != exclude && COMPONENT_INDICATOR_PROPS.contains(it.name) }

    fun isDefaultExports(expression: JSExpression?): Boolean =
      expression is JSReferenceExpression && JSSymbolUtil.isAccurateReferenceExpressionName(expression as JSReferenceExpression?,
                                                                                            JSSymbolUtil.EXPORTS, "default")

    fun getExprReferencedFileUrl(expression: JSExpression?): String? {
      if (expression is JSReferenceExpression) {
        for (resolvedElement in resolveLocally(expression)) {
          (resolvedElement as? ES6ImportedBinding)
            ?.declaration
            ?.fromClause
            ?.let {
              return it.referenceText?.let { ref -> StringUtil.unquoteString(ref) }
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
  }

  override fun findModule(result: PsiElement): PsiElement? = org.jetbrains.vuejs.index.findModule(result)

  override fun interestedProperties(): Array<String> = INTERESTING_PROPERTIES

  override fun processProperty(name: String?, property: JSProperty, out: JSElementIndexingData): Boolean {
    if (MIXINS_PROP == name && property.value is JSArrayLiteralExpression) {
      (property.value as JSArrayLiteralExpression).expressions
        .forEach {
          if (it is JSReferenceExpression) {
            recordMixin(out, property, it, false)
          }
        }
    }
    else if (EXTENDS_PROP == name && property.value is JSReferenceExpression) {
      recordExtends(out, property, property.value)
    }
    //Vuetify typescript components
    else if (NAME_PROP == name && property.value is JSLiteralExpression) {
      val componentName = (property.value as JSLiteralExpression).stringValue
      val obj = property.parent as JSObjectLiteralExpression
      if (componentName != null && obj.containingFile.name.contains(toAsset(componentName),
                                                                    true) && obj.containingFile.fileType is TypeScriptFileType) {
        out.addImplicitElement(createImplicitElement(componentName, property, VueComponentsIndex.JS_KEY))
      }
    }
    else if (TEMPLATE_PROP == name) {
      if (isPossiblyVueContainerInitializer(property.parent as? JSObjectLiteralExpression)) {
        val value = property.value
        if (value is JSLiteralExpression && value.isQuotedLiteral) {
          value.stringValue
            ?.takeIf { it.startsWith("#") && it.length > 1 }
            ?.let {
              JSImplicitElementImpl.Builder(it.substring(1), property)
                .setUserString(VueIdIndex.JS_KEY)
                .forbidAstAccess()
                .toImplicitElement()
            }
            ?.let { out.addImplicitElement(it) }
        }
        else {
          getExprReferencedFileUrl(property.value)
            ?.let { PathUtil.getFileName(it) }
            ?.takeIf { it.isNotBlank() }
            ?.let {
              JSImplicitElementImpl.Builder(it, property)
                .setUserString(VueUrlIndex.JS_KEY)
                .forbidAstAccess()
                .toImplicitElement()
            }
            ?.let { out.addImplicitElement(it) }
        }
      }
    }

    return true
  }

  override fun processAnyProperty(property: JSProperty, outData: JSElementIndexingData?): JSElementIndexingData? {
    val obj = property.parent as? JSObjectLiteralExpression

    var out = outData
    //Bootstrap-vue components
    if (property.containingFile.name == "index.js" && property.parent is JSObjectLiteralExpression) {
      val parent = PsiTreeUtil.findFirstParent(property, Condition {
        return@Condition it is JSVarStatement && it.variables.firstOrNull()?.name == "components"
      })
      if (parent != null) {
        val componentName = property.name ?: ""
        if (out == null) out = JSElementIndexingDataImpl()
        out.addImplicitElement(createImplicitElement(componentName, property, VueComponentsIndex.JS_KEY))
      }
    }

    val firstProperty = obj?.firstProperty ?: return outData
    if (firstProperty == property) {
      val parent = obj.parent
      if (parent is JSExportAssignment ||
          (parent is JSAssignmentExpression && isDefaultExports(parent.definitionExpression?.expression))) {
        if (isPossiblyVueContainerInitializer(obj)) {
          if (out == null) out = JSElementIndexingDataImpl()
          out.addImplicitElement(createImplicitElement(getComponentNameFromDescriptor(obj), property, VueComponentsIndex.JS_KEY))
        }
      }
      else if (((parent as? JSProperty) == null) && isDescriptorOfLinkedInstanceDefinition(obj)) {
        val binding = (obj.findProperty(EL_PROP)?.value as? JSLiteralExpression)?.stringValue
        if (out == null) out = JSElementIndexingDataImpl()
        out.addImplicitElement(createImplicitElement(binding ?: "", property, VueOptionsIndex.JS_KEY))
      }
    }

    return out
  }

  override fun processDecorator(decorator: ES6Decorator, data: JSElementIndexingDataImpl?): JSElementIndexingDataImpl? {
    if (!isComponentDecorator(decorator)) return data

    val exportAssignment = (decorator.parent as? JSAttributeList)?.parent as? ES6ExportDefaultAssignment ?: return data
    if (exportAssignment.stubSafeElement !is JSClassExpression) return data

    val nameProperty = VueComponents.getDescriptorFromDecorator(decorator)?.findProperty(NAME_PROP)
    val name = getTextIfLiteral(nameProperty?.value) ?: FileUtil.getNameWithoutExtension(decorator.containingFile.name)
    val outData = data ?: JSElementIndexingDataImpl()
    outData.addImplicitElement(createImplicitElement(name, decorator, VueComponentsIndex.JS_KEY, null, null, false))
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
        outData.addImplicitElement(createImplicitElement(componentName, callExpression, VueComponentsIndex.JS_KEY,
                                                         nameRefString, arguments[1], true))
      }
    }
    else if (VueStaticMethod.Mixin.matches(reference)) {
      if (arguments.size == 1) {
        recordMixin(outData, callExpression, arguments[0], true)
      }
    }
    else if (VueStaticMethod.Directive.matches(reference)) {
      val directiveName = getTextIfLiteral(arguments[0])
      if (arguments.size >= 2 && !directiveName.isNullOrBlank()) {
        recordDirective(outData, callExpression, directiveName, arguments[1])
      }
    }
    else if (VueStaticMethod.Filter.matches(reference)) {
      val filterName = getTextIfLiteral(arguments[0])
      if (arguments.size >= 2 && !filterName.isNullOrBlank()) {
        val functionDef = arguments[1]
        val nameType = (functionDef as? JSReferenceExpression)?.referenceName
        outData.addImplicitElement(createImplicitElement(
          filterName, callExpression, VueGlobalFiltersIndex.JS_KEY, nameType,
          arguments[1], true))
      }
    }
    else if (reference.referenceName == EXTEND_FUN) {
      when (val qualifier = reference.qualifier) {
        is JSReferenceExpression -> if (
          !qualifier.hasQualifier() && qualifier.referenceName != VUE_NAMESPACE) {
          recordExtends(outData, callExpression, reference.qualifier)
        }
        // 3-rd party library support: vue-typed-mixin
        is JSCallExpression -> {
          val mixinsCall = qualifier.methodExpression?.castSafelyTo<JSReferenceExpression>()
            ?.takeIf { !it.hasQualifier() }
          if (mixinsCall?.referenceName != null
              && JSStubBasedPsiTreeUtil.resolveLocally(mixinsCall.referenceName!!, mixinsCall)
                ?.castSafelyTo<ES6ImportedBinding>()
                ?.context?.castSafelyTo<ES6ImportExportDeclaration>()
                ?.fromClause
                ?.referenceText == "\"vue-typed-mixins\"") {
            for (arg in qualifier.arguments) {
              arg.castSafelyTo<JSReferenceExpression>()
                ?.takeIf { !it.hasQualifier() }
                ?.let { recordExtends(outData, callExpression, it) }
            }
          }
        }
      }
    }
  }

  private fun recordDirective(outData: JSElementIndexingData,
                              provider: JSImplicitElementProvider,
                              directiveName: String,
                              descriptorRef: PsiElement?) {
    outData.addImplicitElement(createImplicitElement(directiveName, provider, VueGlobalDirectivesIndex.JS_KEY,
                                                     null, descriptorRef, true))
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
                            descriptorRef: PsiElement?) {
    outData.addImplicitElement(createImplicitElement(LOCAL, provider, VueExtendsBindingIndex.JS_KEY, null,
                                                     descriptorRef, false))
  }

  private fun isPossiblyVueContainerInitializer(initializer: JSObjectLiteralExpression?): Boolean {
    return initializer != null
           && (initializer.containingFile.fileType == VueFileType.INSTANCE
               || (initializer.containingFile is JSFile && hasComponentIndicatorProperties(initializer)))
  }

  private fun isDescriptorOfLinkedInstanceDefinition(obj: JSObjectLiteralExpression): Boolean {
    val argumentList = obj.parent as? JSArgumentList ?: return false
    if (argumentList.arguments[0] == obj) {
      return JSSymbolUtil.isAccurateReferenceExpressionName(
        (argumentList.parent as? JSNewExpression)?.methodExpression as? JSReferenceExpression, VUE_NAMESPACE) ||
             JSSymbolUtil.isAccurateReferenceExpressionName(
               (argumentList.parent as? JSCallExpression)?.methodExpression as? JSReferenceExpression, VUE_NAMESPACE, EXTEND_FUN)
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
    if (JSElementTypes.ARRAY_LITERAL_EXPRESSION == parentType
        || (JSElementTypes.PROPERTY == parentType
            && expression.node.treeParent.findChildByType(JSTokenTypes.IDENTIFIER)?.text in listOf(PROPS_REQUIRED_PROP, EL_PROP))) {
      return VueFileType.INSTANCE == expression.containingFile.fileType || insideVueDescriptor(expression)
    }
    return false
  }

  // limit building stub in other file types like js/html to Vue-descriptor-like members
  private fun insideVueDescriptor(expression: JSLiteralExpression): Boolean {
    val statement = TreeUtil.findParent(expression.node,
                                        expectedLiteralOwnerExpressions,
                                        JSExtendedLanguagesTokenSetProvider.STATEMENTS) ?: return false
    if (statement.elementType == ES6StubElementTypes.EXPORT_DEFAULT_ASSIGNMENT) return true
    val referenceHolder = if (statement.elementType == JSStubElementTypes.ASSIGNMENT_EXPRESSION)
      statement.findChildByType(JSStubElementTypes.DEFINITION_EXPRESSION)
    else statement
    val ref = referenceHolder?.findChildByType(JSElementTypes.REFERENCE_EXPRESSION) ?: return false
    return ref.getChildren(JSKeywordSets.IDENTIFIER_NAMES).filter { it.text in VUE_DESCRIPTOR_OWNERS }.any()
  }

  private fun getComponentNameFromDescriptor(obj: JSObjectLiteralExpression): String {
    return ((obj.findProperty(NAME_PROP)?.value as? JSLiteralExpression)?.stringValue
            ?: FileUtil.getNameWithoutExtension(obj.containingFile.name))
  }

  override fun indexImplicitElement(element: JSImplicitElementStructure, sink: IndexSink?): Boolean {
    val index = VUE_INDEXES[element.userString]
    if (index != null) {
      sink?.occurrence(index, element.name)
    }
    return index == VueUrlIndex.KEY
  }
}

fun resolveLocally(ref: JSReferenceExpression): List<PsiElement> {
  return if (ref.qualifier == null && ref.referenceName != null) {
    JSStubBasedPsiTreeUtil.resolveLocallyWithMergedResults(ref.referenceName!!, ref)
  }
  else emptyList()
}

fun findModule(element: PsiElement?): JSEmbeddedContent? {
  val file = element as? XmlFile ?: element?.containingFile as? XmlFile
  if (file != null && file.fileType == VueFileType.INSTANCE) {
    val script = findScriptTag(file)
    if (script != null) {
      return PsiTreeUtil.getStubChildOfType(script, JSEmbeddedContent::class.java)
    }
  }
  return null
}

fun findScriptTag(xmlFile: XmlFile): XmlTag? {
  return findTopLevelVueTag(xmlFile, SCRIPT_TAG_NAME)
}

@StubSafe
fun findTopLevelVueTag(xmlFile: XmlFile, tagName: String): XmlTag? {
  if (xmlFile.fileType == VueFileType.INSTANCE) {
    var result: XmlTag? = null
    if (xmlFile is PsiFileImpl) {
      xmlFile.stub?.let { stub ->
        return stub.childrenStubs
          .asSequence()
          .mapNotNull { (it as? XmlTagStub<*>)?.psi }
          .find { it.localName.equals(tagName, ignoreCase = true) }
      }
    }

    xmlFile.accept(object : VueFileVisitor() {
      override fun visitXmlTag(tag: XmlTag?) {
        if (result == null
            && tag != null
            && tag.localName.equals(tagName, ignoreCase = true)) {
          result = tag
        }
      }
    })
    return result
  }
  return null
}

private enum class VueStaticMethod(val methodName: String) {
  Component(COMPONENT_FUN),
  Mixin(MIXIN_FUN),
  Directive(DIRECTIVE_FUN),
  Filter(FILTER_FUN);

  companion object {
    fun matchesAny(reference: JSReferenceExpression): Boolean = values().any { it.matches(reference) }
  }

  fun matches(reference: JSReferenceExpression): Boolean =
    JSSymbolUtil.isAccurateReferenceExpressionName(reference, VUE_NAMESPACE, methodName)
}

open class VueFileVisitor : XmlElementVisitor() {
  override fun visitXmlDocument(document: XmlDocument?): Unit = recursion(document)

  override fun visitXmlFile(file: XmlFile?): Unit = recursion(file)

  protected fun recursion(element: PsiElement?) {
    element?.children?.forEach { it.accept(this) }
  }
}
