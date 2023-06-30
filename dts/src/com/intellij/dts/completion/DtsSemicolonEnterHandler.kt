package com.intellij.dts.completion

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate.Result
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter
import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.psi.DtsEntry
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.registry.Registry
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.endOffset

class DtsSemicolonEnterHandler : EnterHandlerDelegateAdapter() {
    override fun postProcessEnter(file: PsiFile, editor: Editor, dataContext: DataContext): Result {
        if (file !is DtsFile || !Registry.`is`("dts.insert_semicolons")) return Result.Continue

        PsiDocumentManager.getInstance(file.project).commitDocument(editor.document)

        val offset = editor.caretModel.offset
        val iterator = editor.highlighter.createIterator(offset)
        if (iterator.end >= offset && iterator.start > 0) iterator.retreat()

        val element = file.findElementAt(iterator.start)
        val entry = PsiTreeUtil.findFirstParent(element) { it is DtsEntry } as? DtsEntry ?: return Result.Continue
        val statement = entry.dtsStatement

        if (entry.hasDtsSemicolon) return Result.Continue
        if (PsiTreeUtil.hasErrorElements(statement)) return Result.Continue

        editor.document.insertString(statement.endOffset, ";")

        return Result.Default
    }
}