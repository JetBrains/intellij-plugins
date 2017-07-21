package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.javascript.index.FrameworkIndexingHandler
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData
import com.intellij.lang.javascript.psi.stubs.JSImplicitElementStructure
import com.intellij.lang.javascript.psi.stubs.impl.JSElementIndexingDataImpl
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.XmlRecursiveElementWalkingVisitor
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil
import org.jetbrains.vuejs.VueFileType
import org.jetbrains.vuejs.VueLanguage
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

  override fun processAnyProperty(property: JSProperty, outData: JSElementIndexingData?): JSElementIndexingData? {
    val obj = property.parent as JSObjectLiteralExpression

    if ("name" == property.name) {
      val name = (property.value as? JSLiteralExpression)?.value as? String ?: return outData
      property.parent.parent as? JSExportAssignment ?: return outData
      obj.findProperty("template") ?: obj.findProperty("render") ?: property.containingFile.language as? VueLanguage ?: return outData

      val out = outData ?: JSElementIndexingDataImpl()
      out.addImplicitElement(JSImplicitElementImpl.Builder(name, property).setUserString(VueComponentsIndex.JS_KEY).toImplicitElement())
      return out
    } else {
      val name = property.name ?: return outData
      if (OPTIONS.contains(name) && ((obj.parent as? JSProperty) == null)) {
        val out = outData ?: JSElementIndexingDataImpl()
        out.addImplicitElement(JSImplicitElementImpl.Builder(name, property)
                                 .setUserString(VueOptionsIndex.JS_KEY)
                                 .toImplicitElement())
        return out
      }
    }
    return outData
  }

  override fun indexImplicitElement(element: JSImplicitElementStructure, sink: IndexSink?): Boolean {
    INDICES.filter { it.value == element.userString }.forEach { sink?.occurrence(it.key, element.name); return true }
    return false
  }
}