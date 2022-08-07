// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.liveTemplate

import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSSourceElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlText
import com.intellij.psi.xml.XmlTokenType
import com.intellij.xml.util.HtmlUtil
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.lang.html.VueFileType

class VueBaseLiveTemplateContextType : TemplateContextType(VueBundle.message("vue.documentation.vue")) {
  override fun isInContext(file: PsiFile, offset: Int): Boolean {
    return VueFileType.INSTANCE == file.fileType
  }

  companion object {
    fun evaluateContext(file: PsiFile, offset: Int,
                        scriptContextEvaluator: ((PsiElement) -> Boolean)? = null,
                        notVueFileType: ((PsiElement) -> Boolean)? = null,
                        forTagInsert: Boolean = false,
                        forAttributeInsert: Boolean = false): Boolean {
      if (offset < 0) return false
      val element = file.findElementAt(offset) ?: return false

      if (VueFileType.INSTANCE != file.fileType) {
        return isVueContext(file) && notVueFileType != null && notVueFileType.invoke(element)
      }

      val parentTag = PsiTreeUtil.getParentOfType(element, XmlTag::class.java) ?: return false
      if (isTagEnd(element)) {
        return if (HtmlUtil.isScriptTag(parentTag)) scriptContextEvaluator != null && scriptContextEvaluator.invoke(element)
        else forTagInsert
      }

      val parentJS = PsiTreeUtil.getParentOfType(element, JSSourceElement::class.java)
      if (parentJS != null) {
        val embedded = PsiTreeUtil.getParentOfType(parentJS, JSEmbeddedContent::class.java)
        if (embedded != null && embedded.parent is XmlTag && HtmlUtil.isScriptTag(embedded.parent as XmlTag)) {
          return scriptContextEvaluator != null && scriptContextEvaluator.invoke(parentJS)
        }
        return false
      }

      val parentXml = PsiTreeUtil.getParentOfType(element, XmlTag::class.java, XmlText::class.java)
      val isStartTag = element.node.elementType == XmlTokenType.XML_START_TAG_START
      val isOutsideTag = isStartTag || parentXml is XmlText
      return if (isOutsideTag) {
        var tag = parentXml as? XmlTag
        if (tag == null || isStartTag) {
          tag = parentXml?.parent as? XmlTag
        }
        if (HtmlUtil.isScriptTag(tag)) {
          scriptContextEvaluator != null && scriptContextEvaluator.invoke(element)
        }
        else tag != null && forTagInsert
      }
      else {
        parentXml != null && forAttributeInsert
      }
    }

    fun isTagEnd(element: PsiElement): Boolean = element.node.elementType == XmlTokenType.XML_END_TAG_START
  }
}
