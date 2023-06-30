package com.intellij.dts.completion

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.dts.lang.DtsAffiliation
import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.DtsTokenSets
import com.intellij.dts.lang.psi.DtsContainer
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFile

private val rootDirectives = setOf(
    "/dts-v1/",
    "/plugin/",
    "/include/",
    "/memreserve/",
    "/delete-node/",
    "/omit-if-no-ref/",
)

private val nodeDirectives = setOf(
    "/delete-property/",
    "/delete-node/",
    "/include/",
    "/omit-if-no-ref/",
)

class DtsCompilerDirectiveCompletionContributor : CompletionContributor() {
    class AutoPopup : TypedHandlerDelegate() {
        override fun checkAutoPopup(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
            if (file !is DtsFile || c != '/') return Result.CONTINUE

            val element = file.findElementAt(editor.caretModel.offset) ?: return Result.CONTINUE

            when (element.parent) {
                is DtsContainer, is DtsNode -> AutoPopupController.getInstance(project).scheduleAutoPopup(editor, CompletionType.BASIC, null)
            }

            return Result.CONTINUE
        }
    }

    private fun findContainer(position: PsiElement): DtsContainer? {
        return when (val parent = position.parent) {
            is DtsContainer -> parent
            is DtsNode -> parent.dtsContent
            is PsiErrorElement -> parent.parent as? DtsContainer
            else -> null
        }
    }

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        var prefix = ""

        if (parameters.offset > 0) {
            val document = parameters.editor.document
            val iterator = parameters.editor.highlighter.createIterator(parameters.offset)
            if (iterator.end < document.textLength && iterator.start > 0) iterator.retreat()

            // abort if the previous token is a compiler directive
            if (iterator.tokenType in DtsTokenSets.compilerDirectives) return

            if (iterator.tokenType == DtsTypes.NAME) {
                prefix = document.getText(TextRange(iterator.start, iterator.end))

                if (iterator.start > 0) iterator.retreat()
            }

            // or abort if the previous token after the name is a compiler directive
            if (iterator.tokenType in DtsTokenSets.compilerDirectives) return

            if (iterator.tokenType == DtsTypes.SLASH) {
                prefix = document.getText(TextRange(iterator.start, iterator.end)) + prefix

                if (iterator.start > 0) iterator.retreat()
            }
        }

        // abort if not prefixed with /
        if (!prefix.isEmpty() && !prefix.startsWith('/')) return

        // check if part of a dts container
        val container = findContainer(parameters.position) ?: return

        val directives = when (container.dtsAffiliation) {
            DtsAffiliation.ROOT -> rootDirectives
            DtsAffiliation.NODE -> nodeDirectives
            DtsAffiliation.UNKNOWN -> rootDirectives.union(nodeDirectives)
        }

        val set = result.withPrefixMatcher(prefix)
        for (directive in directives) {
            set.addElement(LookupElementBuilder.create(directive))
        }
    }
}