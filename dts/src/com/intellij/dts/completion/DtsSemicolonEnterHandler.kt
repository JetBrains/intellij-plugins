package com.intellij.dts.completion

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate.Result
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter
import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.psi.DtsEntry
import com.intellij.dts.lang.psi.DtsProperty
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.dts.util.DtsUtil
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.registry.Registry
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.refactoring.suggested.endOffset

class DtsSemicolonEnterHandler : EnterHandlerDelegateAdapter() {
    private fun isLineBreak(element: PsiElement): Boolean {
        if (element.elementType != TokenType.WHITE_SPACE) return false
        return element.text.contains('\n')
    }

    private fun skipLabels(property: DtsProperty): Int {
        val lastValue = property.dtsValues.lastOrNull() ?: return property.endOffset

        val children = DtsUtil.children(lastValue, forward = false, unfiltered = true)

        for (window in children.windowed(2)) {
            if (window[0].elementType == TokenType.WHITE_SPACE) continue
            if (window[0].elementType == DtsTypes.LABEL && isLineBreak(window[1])) continue

            return window[0].endOffset
        }

        // fallback, should not be reachable
        Logger.getInstance(this::class.java).warn("skip labels reached end of entry")

        return property.endOffset
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

        if (statement is DtsProperty) {
            editor.document.insertString(skipLabels(statement), ";")
        } else {
            editor.document.insertString(statement.endOffset, ";")
        }

        return Result.Default
    }
}