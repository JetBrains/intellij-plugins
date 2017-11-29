package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.ASTNode
import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.index.FrameworkIndexingHandler
import com.intellij.lang.javascript.index.JSSymbolUtil
import com.intellij.lang.javascript.psi.*
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
import org.jetbrains.vuejs.DIRECTIVES
import org.jetbrains.vuejs.MIXINS
import org.jetbrains.vuejs.VueFileType
import org.jetbrains.vuejs.index.*
import org.jetbrains.vuejs.language.VueJSLanguage
import org.jetbrains.vuejs.language.VueVForExpression

class VueFrameworkHandler : FrameworkIndexingHandler() {
  init {
    INDICES.values.forEach { JSImplicitElementImpl.ourUserStringsRegistry.registerUserString(it) }
  }

  companion object {
    const val VUE = "Vue"
    private val VUE_DESCRIPTOR_OWNERS = arrayOf(VUE, "mixin", "component", "extends", "directive")
  }

  override fun findModule(result: PsiElement): PsiElement? = org.jetbrains.vuejs.codeInsight.findModule(result)

  override fun processAnyProperty(property: JSProperty, outData: JSElementIndexingData?): JSElementIndexingData? {
    val obj = property.parent as JSObjectLiteralExpression

    val out = outData ?: JSElementIndexingDataImpl()
    if (MIXINS == property.name && property.value is JSArrayLiteralExpression) {
      (property.value as JSArrayLiteralExpression).expressions
        .forEach {
          if (it is JSReferenceExpression) {
            recordMixin(out, property, it.text, false)
          } else if (it is JSObjectLiteralExpression && it.firstProperty != null) {
            recordMixin(out, it.firstProperty!!, null, false)
          }
        }
    } else if (DIRECTIVES == property.name) {
      (property.value as? JSObjectLiteralExpression)?.properties?.forEach {
          directive ->
          if (!directive.name.isNullOrBlank()) {
            if (directive.value is JSReferenceExpression) {
              recordDirective(out, directive, directive.name!!, directive.value!!.text, false)
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
          tryProcessComponentInVue(obj, property, out)
        }
      } else if (((obj.parent as? JSProperty) == null) && isDescriptorOfLinkedInstanceDefinition(obj)) {
        val binding = (obj.findProperty("el")?.value as? JSLiteralExpression)?.value as? String
        out.addImplicitElement(JSImplicitElementImpl.Builder(binding ?: "", property)
                                 .setUserString(VueOptionsIndex.JS_KEY)
                                 .toImplicitElement())
      }
    }
    return if (out.isEmpty) outData else out
  }

  private fun hasComponentIndicatorProperties(obj: JSObjectLiteralExpression): Boolean =
    obj.findProperty("template") != null || obj.findProperty("render") != null

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
        if (componentName == null) {
          componentName = tryLibraryComponentNameHeuristics(arguments[0], arguments[1]) ?: return
        }
        val descriptor = arguments[1]
        val provider: PsiElement = (descriptor as? JSObjectLiteralExpression)?.firstProperty ?: callExpression
        // not null type string indicates the global component
        val typeString = (descriptor as? JSReferenceExpression)?.text ?: ""
        outData.addImplicitElement(JSImplicitElementImpl.Builder(componentName, provider)
                                     .setUserString(VueComponentsIndex.JS_KEY)
                                     .setTypeString(typeString)
                                     .toImplicitElement())
      }
    } else if (VueStaticMethod.Mixin.matches(reference)) {
      if (arguments.size == 1) {
        val provider = (arguments[0] as? JSObjectLiteralExpression)?.firstProperty ?: callExpression
        val typeString = (arguments[0] as? JSReferenceExpression)?.text ?: ""
        recordMixin(outData, provider, typeString, true)
      }
    } else if (VueStaticMethod.Directive.matches(reference)) {
      val directiveName = getTextIfLiteral(arguments[0])
      if (arguments.size >= 2 && directiveName != null && !directiveName.isNullOrBlank()) {
        // not null type string indicates the global directive
        val typeString = (arguments[1] as? JSReferenceExpression)?.text ?: ""
        recordDirective(outData, callExpression, directiveName, typeString, true)
      }
    }
  }

  private fun tryLibraryComponentNameHeuristics(nameExp: JSExpression?, descriptorExp: JSExpression?): String? {
    val nameRef = nameExp as? JSReferenceExpression ?: return null
    val descriptorRef = descriptorExp as? JSReferenceExpression ?: return null
    val qualifierRef = nameRef.qualifier as? JSReferenceExpression ?: return null
    if (qualifierRef.qualifier == null && descriptorRef.qualifier == null &&
        qualifierRef.referenceName != null && qualifierRef.referenceName == descriptorRef.referenceName) {
      return qualifierRef.referenceName + "*"
    }
    return null
  }

  private fun recordDirective(outData: JSElementIndexingData,
                              provider: JSImplicitElementProvider,
                              directiveName: String,
                              typeString: String?,
                              isGlobal: Boolean) {
    val index = if (isGlobal) VueGlobalDirectivesIndex.JS_KEY else VueLocalDirectivesIndex.JS_KEY
    outData.addImplicitElement(JSImplicitElementImpl.Builder(directiveName, provider)
                                 .setUserString(index)
                                 .setTypeString(typeString)
                                 .toImplicitElement())
  }

  private fun recordMixin(outData: JSElementIndexingData,
                          provider: JSImplicitElementProvider,
                          typeString: String?,
                          isGlobal: Boolean) {
    outData.addImplicitElement(JSImplicitElementImpl.Builder(if (isGlobal) GLOBAL else LOCAL, provider)
                                 .setUserString(VueMixinBindingIndex.JS_KEY)
                                 .setTypeString(typeString)
                                 .toImplicitElement())
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
                                        TokenSet.create(JSStubElementTypes.CALL_EXPRESSION, JSStubElementTypes.NEW_EXPRESSION),
                                        TokenSet.create(JSElementTypes.EXPRESSION_STATEMENT)) ?: return false
    val ref = statement.findChildByType(JSElementTypes.REFERENCE_EXPRESSION) ?: return false
    return ref.getChildren(TokenSet.create(JSTokenTypes.IDENTIFIER)).filter { it.text in VUE_DESCRIPTOR_OWNERS }.any()
  }

  private fun tryProcessComponentInVue(obj: JSObjectLiteralExpression, property: JSProperty,
                                       outData: JSElementIndexingData) {
    val compName = (obj.findProperty("name")?.value as? JSLiteralExpression)?.value as? String
                   ?: FileUtil.getNameWithoutExtension(obj.containingFile.name)
    outData.addImplicitElement(JSImplicitElementImpl.Builder(compName, property).setUserString(VueComponentsIndex.JS_KEY).toImplicitElement())
  }

  override fun indexImplicitElement(element: JSImplicitElementStructure, sink: IndexSink?): Boolean {
    INDICES.filter { it.value == element.userString }.forEach { sink?.occurrence(it.key, element.name); return true }
    return false
  }

  override fun addTypeFromResolveResult(evaluator: JSTypeEvaluator?, result: PsiElement?, hasSomeType: Boolean): Boolean {
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

fun findModule(result: PsiElement): PsiElement? {
  if (result is XmlFile && result.fileType == VueFileType.INSTANCE) {
    val visitor = MyScriptVisitor()
    result.accept(visitor)
    return visitor.jsElement
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

private class MyScriptVisitor : XmlElementVisitor() {
  internal var jsElement : JSElement? = null
  override fun visitXmlTag(tag: XmlTag?) {
    if (HtmlUtil.isScriptTag(tag)) {
      jsElement = PsiTreeUtil.findChildOfType(tag, JSElement::class.java)
    }
  }

  override fun visitXmlDocument(document: XmlDocument?) = recursion(document)

  override fun visitXmlFile(file: XmlFile?) = recursion(file)

  private fun recursion(document: PsiElement?) {
    document?.children?.forEach { it.accept(this) }
  }
}