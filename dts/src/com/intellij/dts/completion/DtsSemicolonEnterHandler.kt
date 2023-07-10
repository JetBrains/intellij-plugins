package com.intellij.dts.completion

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate.Result
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter
import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.DtsTokenSets
import com.intellij.dts.lang.psi.DtsEntry
import com.intellij.dts.lang.psi.DtsStatement
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.registry.Registry
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.endOffset
import kotlin.math.max
import kotlin.math.min

class DtsSemicolonEnterHandler : EnterHandlerDelegateAdapter() {
    private fun isCommentOrWhitespace(token: IElementType): Boolean {
        return token in DtsTokenSets.comments || token == TokenType.WHITE_SPACE
    }

    private fun findEntry(file: PsiFile, editor: Editor): DtsEntry? {
        val offset = editor.caretModel.offset
        val iterator = editor.highlighter.createIterator(offset)
        if (iterator.end >= offset && iterator.start > 0) iterator.retreat()

        while (iterator.start > 0 && isCommentOrWhitespace(iterator.tokenType)) {
            iterator.retreat()
        }

        val element = file.findElementAt(iterator.start)
        return PsiTreeUtil.findFirstParent(element) { it is DtsEntry } as? DtsEntry
    }

    private fun findInsertPosition(statement: DtsStatement, editor: Editor): Int {
        val iterator = editor.highlighter.createIterator(statement.endOffset)

        while (iterator.start > 0 && isCommentOrWhitespace(iterator.tokenType)) {
            iterator.retreat()
        }

        return iterator.end
    }

    override fun postProcessEnter(file: PsiFile, editor: Editor, dataContext: DataContext): Result {
        if (file !is DtsFile || !Registry.`is`("dts.insert_semicolons")) return Result.Continue

        PsiDocumentManager.getInstance(file.project).commitDocument(editor.document)

        val entry = findEntry(file, editor) ?: return Result.Continue
        val statement = entry.dtsStatement

        // abort if the entry already has a semicolon or the statement has an error
        if (entry.hasDtsSemicolon) return Result.Continue
        if (!statement.dtsIsComplete) return Result.Continue

        // abort if there are more than two linebreaks
        val caretOffset = editor.caretModel.offset
        val insertOffset = findInsertPosition(statement, editor)

        val start = min(insertOffset, caretOffset)
        val end = max(insertOffset, caretOffset)

        if (file.text.subSequence(start, end).count { it == '\n' } > 1) return Result.Continue

        editor.document.insertString(insertOffset, ";")

        return Result.Default
    }
}