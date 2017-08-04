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

val DELIMITERS = "delimiters"
class VueFrameworkHandler : FrameworkIndexingHandler() {
  private val OPTIONS = setOf(DELIMITERS)

  init {
    INDICES.values.forEach { JSImplicitElementImpl.ourUserStringsRegistry.registerUserString(it) }
  }

  override fun findModule(result: PsiElement): PsiElement? {
    return org.jetbrains.vuejs.codeInsight.findModule(result)
  }

  override fun processAnyProperty(property: JSProperty, outData: JSElementIndexingData?): JSElementIndexingData? {
    val obj = property.parent as JSObjectLiteralExpression

    if (obj.parent !is JSProperty && obj.properties[0] == property)  {
      if (obj.parent is JSExportAssignment && obj.containingFile.fileType == VueFileType.INSTANCE) {
        return tryProcessComponentInVue(property, outData)
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

    val name = property.name ?: return outData
    if (OPTIONS.contains(name) && ((obj.parent as? JSProperty) == null)) {
      val out = outData ?: JSElementIndexingDataImpl()
      out.addImplicitElement(JSImplicitElementImpl.Builder(name, property)
                               .setUserString(VueOptionsIndex.JS_KEY)
                               .toImplicitElement())
      return out
    }
    return outData
  }

  override fun shouldCreateStubForLiteral(node: ASTNode?): Boolean {
    if (node?.psi is JSLiteralExpression) {
      return hasSignificantValue(node!!.psi as JSLiteralExpression)
    }
    return super.shouldCreateStubForLiteral(node)
  }

  override fun hasSignificantValue(expression: JSLiteralExpression): Boolean {
    if (expression.containingFile.fileType == VueFileType.INSTANCE) {
      return PsiTreeUtil.getParentOfType(expression, JSArrayLiteralExpression::class.java) != null
    }
    return false
  }

  private fun tryProcessComponentInVue(property: JSProperty,
                                       outData: JSElementIndexingData?): JSElementIndexingData? {
    val compName : String
    if ("name" == property.name) {
      compName = (property.value as? JSLiteralExpression)?.value as? String ?: return outData
    } else {
      compName = FileUtil.getNameWithoutExtension(property.containingFile.name)
    }
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