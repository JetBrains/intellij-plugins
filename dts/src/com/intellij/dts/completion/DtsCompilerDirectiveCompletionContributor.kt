package com.intellij.dts.completion

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.dts.lang.DtsAffiliation
import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.psi.DtsContainer
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
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
        private fun inContainer(position: PsiElement): Boolean {
            if (position is PsiComment) return false

            return when (val parent = position.parent) {
                is DtsContainer, is DtsNode -> true
                is PsiErrorElement -> parent.parent is DtsContainer
                else -> false
            }
        }

        override fun checkAutoPopup(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
            if (file !is DtsFile || c != '/') return Result.CONTINUE

            val element = file.findElementAt(editor.caretModel.offset)
            if (element == null || inContainer(element)) {
                AutoPopupController.getInstance(project).scheduleAutoPopup(editor, CompletionType.BASIC, null)
            }

            return Result.CONTINUE
        }
    }

    private fun findContainer(position: PsiElement): DtsContainer? {
        if (position is PsiComment) return null

        return when (val parent = position.parent) {
            is DtsContainer -> parent
            is DtsNode -> parent.dtsContent
            is PsiErrorElement -> parent.parent as? DtsContainer
            else -> null
        }
    }


    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        val container = findContainer(parameters.position) ?: return

        val directives = when (container.dtsAffiliation) {
            DtsAffiliation.ROOT -> rootDirectives
            DtsAffiliation.NODE -> nodeDirectives
            DtsAffiliation.UNKNOWN -> rootDirectives.union(nodeDirectives)
        }

        val set = result.withDtsPrefixMatcher(parameters)

        for (directive in directives) {
            val lookup = LookupElementBuilder.create(directive)
            set.addElement(PrioritizedLookupElement.withPriority(lookup, DtsLookupPriority.COMPILER_DIRECTIVE))
        }
    }
}