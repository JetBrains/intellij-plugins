package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.intentions.JavaScriptIntention
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlElementType
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.VueFileType

/**
 * @author Irina.Chernushina on 12/13/2017.
 */
class VueExtractComponentIntention : JavaScriptIntention() {
  override fun getFamilyName(): String {
    return VueBundle.message("vue.template.intention.extract.component.family.name")
  }

  override fun getText(): String {
    return VueBundle.message("vue.template.intention.extract.component")
  }

  override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
    editor ?: return false
    if (VueFileType.INSTANCE != element.containingFile?.fileType) return false
    return Companion.getContext(editor, element) != null
  }

  override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
    val context = Companion.getContext(editor, element) ?: return
    VueExtractComponentRefactoring(project, context, editor).perform()
  }

  override fun startInWriteAction() = false

  companion object {
    fun getContext(editor: Editor?, element: PsiElement): List<XmlTag>? {
      val file = element.containingFile
      if (file == null) return null
      if (editor == null || !editor.selectionModel.hasSelection()) {
        val type = element.node.elementType
        val parent = element.parent as? XmlTag
        if (parent != null && (type == XmlElementType.XML_NAME || type == XmlElementType.XML_START_TAG_START)) {
          return listOf(parent)
        }
        if (element is XmlTag) return listOf(element)
        return null
      }
      var start = editor.selectionModel.selectionStart
      val end = editor.selectionModel.selectionEnd

      val list = mutableListOf<XmlTag>()
      while (start < end) {
        while (file.findElementAt(start) is PsiWhiteSpace && start < end) start++
        if (start == end) break
        val tag = PsiTreeUtil.findElementOfClassAtOffset(file, start, XmlTag::class.java, true) ?: return null
        list.add(tag)
        start = tag.textRange.endOffset
      }
      if (list.isEmpty()) return null
      return list
    }
  }
}