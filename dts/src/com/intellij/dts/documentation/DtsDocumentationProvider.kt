package com.intellij.dts.documentation

import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.DtsProperty
import com.intellij.dts.lang.psi.getDtsAnnotationTarget
import com.intellij.dts.util.DtsTreeUtil
import com.intellij.dts.util.DtsUtil
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.DocumentationTargetProvider
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil

/**
 * Implements [DocumentationTargetProvider] because not all psi elements which
 * should have documentation are passed to the [PsiDocumentationTargetProvider]
 * callback.
 *
 * Implements [PsiDocumentationTargetProvider] to provide documentation for
 * the autocomplete popup.
 */
class DtsDocumentationProvider : DocumentationTargetProvider, PsiDocumentationTargetProvider {
  override fun documentationTargets(file: PsiFile, offset: Int): List<DocumentationTarget> {
    return DtsUtil.singleResult {
      if (file !is DtsFile) return@singleResult null

      val element = findTargetElement(file, offset)
                    ?: findTargetElement(file, offset - 1)
                    ?: return@singleResult null

      createTarget(element)
    }
  }

  override fun documentationTarget(element: PsiElement, originalElement: PsiElement?): DocumentationTarget? {
    return createTarget(element)
  }

  private fun createTarget(element: PsiElement): DocumentationTarget? {
    return when (element) {
      is DtsNode -> DtsNodeDocumentationTarget(element)
      is DtsProperty -> DtsPropertyDocumentationTarget(element)
      else -> null
    }
  }

  private fun findTargetElement(file: PsiFile, offset: Int): PsiElement? {
    if (offset < 0) return null

    val originalElement = file.findElementAt(offset) ?: return null
    return findTargetElement(originalElement)
  }

  private fun findTargetElement(originalElement: PsiElement): PsiElement? {
    // annotation targets of statements
    val statement = DtsTreeUtil.parentStatement(originalElement)
    val partOfTarget = PsiTreeUtil.isAncestor(statement?.getDtsAnnotationTarget(), originalElement, false)
    if (partOfTarget) return statement

    // references to statements
    val referenceHost = PsiTreeUtil.findFirstParent(originalElement, false) { it.reference != null }
    return referenceHost?.reference?.resolve()
  }
}