package org.intellij.plugins.markdown.lang.psi

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement

open class MarkdownRecursiveElementVisitor : MarkdownElementVisitor() {
  override fun visitElement(element: PsiElement) {
    ProgressManager.checkCanceled()
    element.acceptChildren(this)
  }
}
