package com.intellij.dts.completion

import com.intellij.codeInsight.CodeInsightSettings
import com.intellij.codeInsight.editorActions.BackspaceHandlerDelegate
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.codeInsight.editorActions.TypedHandlerUtil
import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.TokenSet

// implementation from com.intellij.codeInsight.editorActions.JavaTypedHandler
class DtsAngularBraceTypedHandler : TypedHandlerDelegate() {
    private var lAngleTyped = false
    override fun beforeCharTyped(c: Char, project: Project, editor: Editor, file: PsiFile, fileType: FileType): Result {
        if (file !is DtsFile || !CodeInsightSettings.getInstance().AUTOINSERT_PAIR_BRACKET) return Result.CONTINUE

        lAngleTyped = c == '<'

        if (c == '>' && TypedHandlerUtil.handleGenericGT(editor, DtsTypes.LANGL, DtsTypes.RANGL, TokenSet.EMPTY)) {
            return Result.STOP
        }

        return Result.CONTINUE
    }

    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        if (file !is DtsFile || !CodeInsightSettings.getInstance().AUTOINSERT_PAIR_BRACKET) return Result.CONTINUE

        if (lAngleTyped) {
            lAngleTyped = false
            TypedHandlerUtil.handleAfterGenericLT(editor, DtsTypes.LANGL, DtsTypes.RANGL, TokenSet.EMPTY)

            return Result.STOP
        }

        return Result.CONTINUE
    }
}

// implementation from com.intellij.codeInsight.editorActions.JavaBackspaceHandler
class DtsAngularBraceBackspaceHandler : BackspaceHandlerDelegate() {
    private var beforeLAngle = false

    override fun beforeCharDeleted(c: Char, file: PsiFile, editor: Editor) {
        if (file !is DtsFile) return

        beforeLAngle = c == '<'
    }

    override fun charDeleted(c: Char, file: PsiFile, editor: Editor): Boolean {
        if (file !is DtsFile || !beforeLAngle || c != '<') return false

        val offset: Int = editor.caretModel.offset
        val chars: CharSequence = editor.document.charsSequence

        if (editor.document.textLength <= offset) return false // virtual space after end of file
        if (chars[offset] != '>') return true

        TypedHandlerUtil.handleGenericLTDeletion(editor, offset, DtsTypes.LANGL, DtsTypes.RANGL, TokenSet.EMPTY)

        return true
    }
}