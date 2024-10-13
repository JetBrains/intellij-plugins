package org.jetbrains.qodana.staticAnalysis.inspections.metrics

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor

/**
 * Recursively iterates elements in a Psi tree by implementing preorder traversal.
 * Children of the currently traversed [PsiElement] will not be visited if [returnCondition] is true.
 * The [beforeReturnCallback] will be executed before returning from visitElement function (before visiting its children).
 *
 * @param visitor visitor that is called on each child node.
 * @param returnCondition condition that indicates when to stop traversal of child nodes.
 * @param beforeReturnCallback callback that will be executed before stopping the traversal of child nodes.
 */
fun PsiFile.iterateFileContents(
  visitor: PsiElementVisitor,
  returnCondition: (PsiElement) -> Boolean = { false },
  beforeReturnCallback: (PsiElement) -> Unit = {},
) {
  this.accept(object : PsiRecursiveElementVisitor() {
    override fun visitElement(element: PsiElement) {
      ProgressManager.checkCanceled()

      if (returnCondition(element)) {
        beforeReturnCallback(element)
        return
      }

      var child = element.firstChild
      while (child != null) {
        child.accept(visitor)
        child.accept(this)
        child = child.nextSibling
      }
    }
  })
}