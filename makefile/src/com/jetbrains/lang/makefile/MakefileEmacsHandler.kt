package com.jetbrains.lang.makefile

import com.intellij.codeInsight.editorActions.emacs.EmacsProcessingHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.util.DocumentUtil

class MakefileEmacsHandler : EmacsProcessingHandler {
    override fun changeIndent(project: Project, editor: Editor, file: PsiFile): EmacsProcessingHandler.Result {
        val selectionModel = editor.selectionModel
        if (selectionModel.hasSelection()) {
            return EmacsProcessingHandler.Result.CONTINUE
        }

        // make-mode.el just inserts a tab character.
        // auto indentation is left for a to-do.
        val caretOffset: Int = editor.caretModel.offset
        editor.document.insertString(caretOffset, "\t")
        editor.caretModel.moveToOffset(caretOffset + 1)

        return EmacsProcessingHandler.Result.STOP
    }
}
