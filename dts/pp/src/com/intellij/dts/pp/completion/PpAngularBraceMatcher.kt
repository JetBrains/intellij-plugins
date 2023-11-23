package com.intellij.dts.pp.completion

import com.intellij.codeInsight.CodeInsightSettings
import com.intellij.codeInsight.editorActions.BackspaceHandlerDelegate
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.codeInsight.editorActions.TypedHandlerUtil
import com.intellij.lang.Language
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

// implementation from com.intellij.codeInsight.editorActions.JavaTypedHandler
abstract class PpAngularBraceTypedHandler : TypedHandlerDelegate() {
  private var lAngleTyped = false

  protected abstract fun getLanguage(): Language

  protected abstract fun getPairs(): List<Pair<IElementType, IElementType>>

  override fun beforeCharTyped(c: Char, project: Project, editor: Editor, file: PsiFile, fileType: FileType): Result {
    if (file.language != getLanguage() || !CodeInsightSettings.getInstance().AUTOINSERT_PAIR_BRACKET) return Result.CONTINUE

    lAngleTyped = c == '<'
    if (c != '>') return Result.CONTINUE

    for (pair in getPairs()) {
      if (TypedHandlerUtil.handleGenericGT(editor, pair.first, pair.second, TokenSet.EMPTY)) {
        return Result.STOP
      }
    }

    return Result.CONTINUE
  }

  override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
    if (!lAngleTyped || file.language != getLanguage() || !CodeInsightSettings.getInstance().AUTOINSERT_PAIR_BRACKET) return Result.CONTINUE

    lAngleTyped = false

    for (pair in getPairs()) {
      TypedHandlerUtil.handleAfterGenericLT(editor, pair.first, pair.second, TokenSet.EMPTY)
    }

    return Result.STOP
  }
}

// implementation from com.intellij.codeInsight.editorActions.JavaBackspaceHandler
abstract class PpAngularBraceBackspaceHandler : BackspaceHandlerDelegate() {
  private var beforeLAngle = false

  protected abstract fun getLanguage(): Language

  protected abstract fun getPairs(): List<Pair<IElementType, IElementType>>

  override fun beforeCharDeleted(c: Char, file: PsiFile, editor: Editor) {
    if (file.language != getLanguage()) return

    beforeLAngle = c == '<'
  }

  override fun charDeleted(c: Char, file: PsiFile, editor: Editor): Boolean {
    if (!beforeLAngle || c != '<' || file.language != getLanguage()) return false

    val offset: Int = editor.caretModel.offset
    val chars: CharSequence = editor.document.charsSequence

    if (editor.document.textLength <= offset) return false // virtual space after end of file
    if (chars[offset] != '>') return true

    for (pair in getPairs()) {
      TypedHandlerUtil.handleGenericLTDeletion(editor, offset, pair.first, pair.second, TokenSet.EMPTY)
    }

    return true
  }
}
