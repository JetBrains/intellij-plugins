// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.formatting

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil

class CaretSnapshot(
  val primary: RangeMarker,
  val secondary: RangeMarker?
) {
  fun restore(document: Document, psiFile: PsiFile) {
    try {
      FileEditorManager.getInstance(psiFile.project).selectedTextEditor?.let { editor ->
        if (editor.document !== document) return@let
        val offset = when {
          primary.isValid -> primary.startOffset
          secondary != null && secondary.isValid -> secondary.endOffset
          else -> -1
        }
        if (offset > 0) editor.caretModel.moveToOffset(offset)
      }
    }
    finally {
      primary.dispose()
      secondary?.dispose()
    }
  }

  companion object {
    fun from(document: Document, psiFile: PsiFile): CaretSnapshot? {
      val editor = FileEditorManager.getInstance(psiFile.project).selectedTextEditor ?: return null
      if (editor.document !== document) return null
      val offset = editor.caretModel.offset
      val element = psiFile.findElementAt(offset) ?: psiFile
      val primary = document.createRangeMarker(offset, offset)
      val secondary = if (element is PsiWhiteSpace) PsiTreeUtil.skipWhitespacesBackward(element)?.let { document.createRangeMarker(it.textRange) } else null
      return CaretSnapshot(primary, secondary)
    }
  }
}
