package com.intellij.dts.lang.resolve

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.dts.DtsIcons
import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.psi.*
import com.intellij.dts.util.DtsPath
import com.intellij.dts.util.DtsTreeUtil
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.refactoring.suggested.startOffset
import kotlin.math.max

/**
 * Same as [DtsLabelReference] but for path references.
 *
 * @param value whether the reference is used as a property value
 */
class DtsPathReference(
    element: PsiElement,
    rangeInElement: TextRange,
    private val path: DtsPath,
    private val value: Boolean,
) : PsiPolyVariantReferenceBase<PsiElement>(element, rangeInElement, false) {
    class AutoPopup : TypedHandlerDelegate() {
        override fun checkAutoPopup(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
            if (file !is DtsFile || c != '/') return Result.CONTINUE

            val element = file.findElementAt(max(editor.caretModel.offset - 1, 0))
            if (element?.parent is DtsPHandle) {
                AutoPopupController.getInstance(project).scheduleAutoPopup(editor, CompletionType.BASIC, null)
            }

            return Result.CONTINUE
        }
    }

    private fun search(path: DtsPath, callback: (DtsNode) -> Unit) {
        val offset = if (value) null else element.startOffset

        DtsTreeUtil.search(element.containingFile, path, offset) { node ->
            callback(node)

            // return null to continue searching through all nodes
            null
        }
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val result = mutableListOf<PsiElement>()
        search(path) { node ->
            if (node !is DtsRefNode) {
                result.add(node)
            }
        }

        return result.map(::PsiElementResolveResult).toTypedArray()
    }

    override fun getVariants(): Array<Any> {
        val variants = mutableListOf<LookupElementBuilder>()
        search(path.parent()) { node ->
            for (subNode in node.dtsSubNodes) {
                val path = subNode.getDtsPath() ?: continue

                val lookup = LookupElementBuilder.create(path)
                    .withTypeText(subNode.getDtsPresentableText())
                    .withPsiElement(subNode)
                    .withIcon(DtsIcons.Node)

                variants.add(lookup)
            }
        }

        return variants.toTypedArray()
    }
}