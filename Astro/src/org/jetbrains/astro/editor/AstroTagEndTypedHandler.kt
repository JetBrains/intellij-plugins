// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.editor

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.xml.XmlTokenType
import org.jetbrains.astro.lang.psi.AstroHtmlTag

class AstroTagEndTypedHandler : TypedHandlerDelegate() {
  override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
    if (c != '>') return Result.CONTINUE

    PsiDocumentManager.getInstance(project).commitDocument(editor.document)

    val offset = editor.caretModel.offset
    var elementAtCaret = file.findElementAt(offset)
    if (elementAtCaret == null || elementAtCaret.elementType == XmlTokenType.XML_END_TAG_START)
      elementAtCaret = file.findElementAt(offset - 1) ?: return Result.CONTINUE

    val astroHtmlTag = PsiTreeUtil.getParentOfType(elementAtCaret, AstroHtmlTag::class.java) ?: return Result.CONTINUE
    when (astroHtmlTag.lastChild) {
      // AstroHtmlTag is not closed (and not a closing element itself).
      is PsiErrorElement -> {
        editor.document.insertString(offset, "</${astroHtmlTag.name}>")
        return Result.STOP
      }
      else -> return Result.CONTINUE
    }
  }
}