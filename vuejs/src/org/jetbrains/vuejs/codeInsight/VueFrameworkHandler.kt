package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.ASTNode
import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.javascript.index.FrameworkIndexingHandler
import com.intellij.lang.javascript.index.JSSymbolUtil
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData
import com.intellij.lang.javascript.psi.stubs.JSImplicitElementStructure
import com.intellij.lang.javascript.psi.stubs.impl.JSElementIndexingDataImpl
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.XmlRecursiveElementWalkingVisitor
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil
import org.jetbrains.vuejs.VueFileType
import org.jetbrains.vuejs.index.INDICES
import org.jetbrains.vuejs.index.VueComponentsIndex
import org.jetbrains.vuejs.index.VueOptionsIndex

class VueFrameworkHandler : FrameworkIndexingHandler() {
  init {
    INDICES.values.forEach { JSImplicitElementImpl.ourUserStringsRegistry.registerUserString(it) }
  }

  override fun findModule(result: PsiElement): PsiElement? = org.jetbrains.vuejs.codeInsight.findModule(result)

  override fun processAnyProperty(property: JSProperty, outData: JSElementIndexingData?): JSElementIndexingData? {
    val obj = property.parent as JSObjectLiteralExpression

    if (obj.parent !is JSProperty && obj.properties[0] == property)  {
      if (obj.parent is JSExportAssignment && obj.containingFile.fileType == VueFileType.INSTANCE) {
        return tryProcessComponentInVue(obj, property, outData)
      }
      else {
        val componentName = tryGetNameFromComponentDefinition(obj)
        if (componentName != null) {
          val out = outData ?: JSElementIndexingDataImpl()
          out.addImplicitElement(JSImplicitElementImpl.Builder(componentName, property)
                                   .setUserString(VueComponentsIndex.JS_KEY).toImplicitElement())
          return out
        }
      }
    }

    if (obj.properties[0] == property && ((obj.parent as? JSProperty) == null) && isDescriptorOfLinkedInstanceDefinition(obj)) {
      val out = outData ?: JSElementIndexingDataImpl()
      val binding = (obj.findProperty("el")?.value as? JSLiteralExpression)?.value as? String
      out.addImplicitElement(JSImplicitElementImpl.Builder(binding ?: "", property)
                               .setUserString(VueOptionsIndex.JS_KEY)
                               .toImplicitElement())
      return out
    }
    return outData
  }

  private fun isDescriptorOfLinkedInstanceDefinition(obj: JSObjectLiteralExpression): Boolean {
    val argumentList = obj.parent as? JSArgumentList ?: return false
    if (argumentList.arguments[0] == obj) {
      return JSSymbolUtil.isAccurateReferenceExpressionName(
          (argumentList.parent as? JSNewExpression)?.methodExpression as? JSReferenceExpression, "Vue") ||
        JSSymbolUtil.isAccurateReferenceExpressionName(
          (argumentList.parent as? JSCallExpression)?.methodExpression as? JSReferenceExpression, "Vue", "extends")
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
    if (expression.containingFile.fileType == VueFileType.INSTANCE || insideVueDescriptor(expression)) {
      return PsiTreeUtil.getParentOfType(expression, JSArrayLiteralExpression::class.java) != null ||
             "required" == (expression.parent as? JSProperty)?.name
    }
    return false
  }

  // limit building stub in other file types like js/html to Vue-descriptor-like members
  private fun insideVueDescriptor(expression: JSLiteralExpression): Boolean {
    val statement = PsiTreeUtil.getParentOfType(expression, JSSourceElement::class.java) ?: return false
    return statement is JSExpressionStatement &&
           PsiTreeUtil.findChildrenOfType(statement, JSReferenceExpression::class.java).firstOrNull { "Vue" == it.referenceName } != null
  }

  private fun tryProcessComponentInVue(obj: JSObjectLiteralExpression, property: JSProperty,
                                       outData: JSElementIndexingData?): JSElementIndexingData? {
    val compName = (obj.findProperty("name")?.value as? JSLiteralExpression)?.value as? String
                   ?: FileUtil.getNameWithoutExtension(obj.containingFile.name)
    val out = outData ?: JSElementIndexingDataImpl()
    out.addImplicitElement(JSImplicitElementImpl.Builder(compName, property).setUserString(VueComponentsIndex.JS_KEY).toImplicitElement())

    return out
  }

  private fun tryGetNameFromComponentDefinition(obj : JSObjectLiteralExpression) : String? {
    val callExpression = (obj.parent as? JSArgumentList)?.parent as? JSCallExpression ?: return null
    if (callExpression.methodExpression is JSReferenceExpression &&
        JSSymbolUtil.isAccurateReferenceExpressionName(callExpression.methodExpression as JSReferenceExpression, "Vue", "component")) {
      val callArgs = callExpression.arguments
      if (callArgs.size > 1 && callArgs[1] == obj &&
          callArgs[0] is JSLiteralExpression && (callArgs[0] as JSLiteralExpression).isQuotedLiteral) {
        return StringUtil.unquoteString((callArgs[0] as JSLiteralExpression).text)
      }
    }
    return null
  }

  override fun indexImplicitElement(element: JSImplicitElementStructure, sink: IndexSink?): Boolean {
    INDICES.filter { it.value == element.userString }.forEach { sink?.occurrence(it.key, element.name); return true }
    return false
  }
}

fun findModule(result: PsiElement): PsiElement? {
  if (result is XmlFile) {
    if (result.fileType == VueFileType.INSTANCE) {
      val ref = Ref.create<JSElement>()
      result.accept(object : XmlRecursiveElementWalkingVisitor() {
        override fun visitXmlTag(tag: XmlTag?) {
          if (HtmlUtil.isScriptTag(tag)) {
            ref.set(PsiTreeUtil.findChildOfType(tag, JSElement::class.java))
            return
          }
        }
      })
      return ref.get()
    }
  }
  return null
}