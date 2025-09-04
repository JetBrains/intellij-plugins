package com.intellij.dts.lang.resolve

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.dts.DtsIcons
import com.intellij.dts.api.DtsNodeVisitor
import com.intellij.dts.api.DtsPath
import com.intellij.dts.api.dtsAccept
import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.psi.*
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.startOffset
import com.intellij.util.asSafely
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
        AutoPopupController.getInstance(project).scheduleAutoPopup(editor)
      }

      return Result.CONTINUE
    }
  }

  private fun search(path: DtsPath, callback: (DtsNode) -> Unit) {
    val file = element.containingFile.asSafely<DtsFile>() ?: return

    val visitor = object : DtsNodeVisitor {
      override fun visit(node: DtsNode): Boolean {
        callback(node)
        return false
      }
    }

    file.dtsAccept(
      visitor,
      path,
      forward = false,
      maxOffset = if (value) null else element.startOffset,
    )
  }

  override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
    val result = mutableListOf<PsiElement>()

    search(path) { node ->
      if (node !is DtsRefNode) result.add(node)
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