package com.intellij.dts.documentation

import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.DtsProperty
import com.intellij.dts.lang.psi.DtsStatement
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.DocumentationTargetProvider
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType

/**
 * Implements [DocumentationTargetProvider] because not all psi elements which
 * should have documentation are passed to the [PsiDocumentationTargetProvider]
 * callback.
 *
 * Implements [PsiDocumentationTargetProvider] to provide documentation for
 * the autocomplete popup.
 */
class DtsDocumentationProvider : DocumentationTargetProvider, PsiDocumentationTargetProvider {
    override fun documentationTargets(file: PsiFile, offset: Int): MutableList<out DocumentationTarget> {
        if (file !is DtsFile) return mutableListOf()

        val originalElement = file.findElementAt(offset) ?: return mutableListOf()
        if (originalElement.elementType != DtsTypes.NAME) return mutableListOf()

        val element = findTargetElement(originalElement) ?: return mutableListOf()
        val documentation = documentationTarget(element, originalElement) ?: return mutableListOf()

        return mutableListOf(documentation)
    }

    override fun documentationTarget(element: PsiElement, originalElement: PsiElement?): DocumentationTarget? {
        return when (element) {
            is DtsNode -> DtsNodeDocumentationTarget(element)
            is DtsProperty -> DtsPropertyDocumentationTarget(element)
            else -> null
        }
    }

    private fun findTargetElement(originalElement: PsiElement): PsiElement? {
        var element: PsiElement? = originalElement
        while (element != null) {
            if (element is DtsStatement) return element

            val reference = element.reference?.resolve()
            if (reference != null) return reference

            element = element.getParent()
        }

        return null
    }
}