package com.intellij.dts.completion

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate.Result
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter
import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.psi.DtsEntry
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.dts.util.DtsUtil
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.registry.Registry
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset

class DtsSemicolonEnterHandler : EnterHandlerDelegateAdapter() {
    private fun isLineBreak(element: PsiElement): Boolean {
        if (element.elementType != TokenType.WHITE_SPACE) return false
        return element.text.contains('\n')
    }

    private fun skippableElement(element: PsiElement): Boolean {
        if (element.elementType == DtsTypes.LABEL) return true

        // find first unproductive element or stop the search at the entry element
        val unproductiveElement = PsiTreeUtil.findFirstParent(element, false) {
            it is DtsEntry || !DtsUtil.isProductiveElement(it.elementType)
        }

        return unproductiveElement != null && unproductiveElement !is DtsEntry
    }

    private fun skipElements(file: PsiFile, editor: Editor, statement: PsiElement): Int {
        val iterator = editor.highlighter.createIterator(statement.endOffset)

        while (iterator.start > statement.startOffset) {
            val element = file.findElementAt(iterator.start) ?: break

            if (!skippableElement(element)) {
                return iterator.end
            } else {
                iterator.retreat()
            }
        }

        return iterator.end
    }

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

        editor.document.insertString(skipElements(file, editor, statement), ";")

        return Result.Default
    }
}