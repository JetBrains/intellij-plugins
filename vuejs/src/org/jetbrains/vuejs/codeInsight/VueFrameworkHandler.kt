// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.ASTNode
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.ecmascript6.psi.JSClassExpression
import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.index.FrameworkIndexingHandler
import com.intellij.lang.javascript.index.JSSymbolUtil
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData
import com.intellij.lang.javascript.psi.stubs.JSImplicitElementStructure
import com.intellij.lang.javascript.psi.stubs.impl.JSElementIndexingDataImpl
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.impl.source.tree.TreeUtil
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil
import org.jetbrains.vuejs.VueFileType
import org.jetbrains.vuejs.codeInsight.VueComponents.Companion.isComponentDecorator
import org.jetbrains.vuejs.index.*
import org.jetbrains.vuejs.language.VueJSLanguage
import org.jetbrains.vuejs.language.VueVForExpression

class VueFrameworkHandler : FrameworkIndexingHandler() {
  init {
    INDICES.values.forEach { JSImplicitElementImpl.ourUserStringsRegistry.registerUserString(it) }
  }

  companion object {
    const val VUE = "Vue"
    private val VUE_DESCRIPTOR_OWNERS = arrayOf(VUE, "mixin", "component", "extends", "directive", "delimiters")
    private val COMPONENT_INDICATOR_PROPS = setOf("template", "render", "mixins", "components", "props")
    fun hasComponentIndicatorProperties(obj: JSObjectLiteralExpression): Boolean =
      obj.properties.any { Companion.COMPONENT_INDICATOR_PROPS.contains(it.name) }
  }

  override fun findModule(result: PsiElement): PsiElement? = org.jetbrains.vuejs.codeInsight.findModule(result)

  override fun processAnyProperty(property: JSProperty, outData: JSElementIndexingData?): JSElementIndexingData? {
    val obj = property.parent as JSObjectLiteralExpression

    val out = outData ?: JSElementIndexingDataImpl()
    if (MIXINS == property.name && property.value is JSArrayLiteralExpression) {
      (property.value as JSArrayLiteralExpression).expressions
        .forEach {
          if (it is JSReferenceExpression) {
            recordMixin(out, property, it, false)
          }
          else if (it is JSObjectLiteralExpression && it.firstProperty != null) {
            recordMixin(out, it.firstProperty!!, null, false)
          }
        }
    } else if (EXTENDS == property.name && property.value is JSReferenceExpression) {
      recordExtends(out, property, property.value, false)
    } else if (DIRECTIVES == property.name) {
      (property.value as? JSObjectLiteralExpression)?.properties?.forEach {
          directive ->
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

    val firstProperty = obj.firstProperty ?: return outData
    if (firstProperty == property) {
      if (obj.parent is JSExportAssignment) {
        if (obj.containingFile.fileType == VueFileType.INSTANCE || obj.containingFile is JSFile && hasComponentIndicatorProperties(obj)) {
          out.addImplicitElement(createImplicitElement(getComponentNameFromDescriptor(obj), property, VueComponentsIndex.JS_KEY))
        }
      } else if (((obj.parent as? JSProperty) == null) && isDescriptorOfLinkedInstanceDefinition(obj)) {
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
    val name = getTextIfLiteral(nameProperty?.value) ?:
               FileUtil.getNameWithoutExtension(decorator.containingFile.name)
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
    } else if (VueStaticMethod.Mixin.matches(reference)) {
      if (arguments.size == 1) {
        val provider = (arguments[0] as? JSObjectLiteralExpression)?.firstProperty ?: callExpression
        recordMixin(outData, provider, arguments[0], true)
      }
    } else if (VueStaticMethod.Directive.matches(reference)) {
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
                                        TokenSet.create(JSStubElementTypes.CALL_EXPRESSION, JSStubElementTypes.NEW_EXPRESSION,
                                                        JSStubElementTypes.ASSIGNMENT_EXPRESSION),
                                        TokenSet.create(JSElementTypes.EXPRESSION_STATEMENT)) ?: return false
    val referenceHolder = if (statement.elementType == JSStubElementTypes.ASSIGNMENT_EXPRESSION)
      statement.findChildByType(JSStubElementTypes.DEFINITION_EXPRESSION)
    else statement
    val ref = referenceHolder?.findChildByType(JSElementTypes.REFERENCE_EXPRESSION) ?: return false
    return ref.getChildren(TokenSet.create(JSTokenTypes.IDENTIFIER)).filter { it.text in VUE_DESCRIPTOR_OWNERS}.any()
  }

  private fun getComponentNameFromDescriptor(obj: JSObjectLiteralExpression): String {
    return ((obj.findProperty("name")?.value as? JSLiteralExpression)?.stringValue
     ?: FileUtil.getNameWithoutExtension(obj.containingFile.name))
  }

  override fun indexImplicitElement(element: JSImplicitElementStructure, sink: IndexSink?): Boolean {
    INDICES.filter { it.value == element.userString }.forEach { sink?.occurrence(it.key, element.name); return true }
    return false
  }

  override fun addTypeFromResolveResult(evaluator: JSTypeEvaluator?, result: PsiElement?): Boolean {
    if (result == null || evaluator == null || !org.jetbrains.vuejs.index.hasVue(result.project)) return false
    if (result is JSVariable && result.language is VueJSLanguage) {
      val vFor = result.parent as? VueVForExpression ?: result.parent.parent as? VueVForExpression
      val vForRef = vFor?.getReferenceExpression()
      val variables = vFor?.getVarStatement()?.variables
      if (vForRef != null && variables != null && !variables.isEmpty() && result == variables[0]) {
        if (JSPsiImplUtils.calculateTypeOfVariableForIteratedExpression(evaluator, vForRef, vFor)) return true
      }
    }
    return false
  }
}

fun findModule(element: PsiElement?): JSEmbeddedContent? {
  val file = element?.containingFile as? XmlFile
  if (file != null && file.fileType == VueFileType.INSTANCE) {
    val script = findScriptTag(file)
    if (script != null) {
      return PsiTreeUtil.findChildOfType(script, JSEmbeddedContent::class.java)
    }
  }
  return null
}

fun findScriptTag(xmlFile : XmlFile): XmlTag? {
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

open class VueFileVisitor: XmlElementVisitor() {
  override fun visitXmlDocument(document: XmlDocument?) = recursion(document)

  override fun visitXmlFile(file: XmlFile?) = recursion(file)

  protected fun recursion(element: PsiElement?) {
    element?.children?.forEach { it.accept(this) }
  }
}