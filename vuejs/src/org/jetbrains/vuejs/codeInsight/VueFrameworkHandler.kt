package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.index.FrameworkIndexingHandler
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.XmlRecursiveElementWalkingVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil
import org.jetbrains.vuejs.VueFileType

class VueFrameworkHandler : FrameworkIndexingHandler() {
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
}