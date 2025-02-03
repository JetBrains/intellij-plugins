// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.intentions.extractComponent

import com.intellij.lang.javascript.intentions.JavaScriptIntention
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlTokenType
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.lang.html.isVueFile

class VueExtractComponentIntention : JavaScriptIntention() {
  override fun getFamilyName(): String {
    return VueBundle.message("vue.template.intention.extract.component.family.name")
  }

  override fun getText(): String {
    return VueBundle.message("action.VueExtractComponentAction.text")
  }

  override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
    editor ?: return false
    if (!element.containingFile.isVueFile) return false
    return getContextForExtractComponentIntention(editor, element) != null
  }

  override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
    editor ?: return
    val context = getContextForExtractComponentIntention(editor, element) ?: return
    VueExtractComponentRefactoring(project, context, editor).perform()
  }

  override fun startInWriteAction(): Boolean = false
}

fun getContextForExtractComponentIntention(editor: Editor?, element: PsiElement): List<XmlTag>? {
  val selectedTags = getSelectedTags(element, editor)
  return if (selectedTags == null || selectedTags.any {
      PsiTreeUtil.getParentOfType(it, XmlTag::class.java) == null
    }) return null
  else selectedTags
}

private fun getSelectedTags(
  element: PsiElement,
  editor: Editor?,
): List<XmlTag>? {
  val file = element.containingFile
  if (file == null) return null
  element.node ?: return null
  if (editor == null || !editor.selectionModel.hasSelection()) {
    val type = element.node.elementType
    val parent = element.parent as? XmlTag
    if (parent != null && (type == XmlTokenType.XML_NAME ||
                           type == XmlTokenType.XML_START_TAG_START ||
                           type == XmlTokenType.XML_TAG_NAME)) {
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
    val textRange = tag.textRange
    if (textRange.startOffset !in start until end) break
    if (textRange.endOffset > end) return null
    list.add(tag)
    start = textRange.endOffset
  }
  if (list.isEmpty()) return null
  return list
}
