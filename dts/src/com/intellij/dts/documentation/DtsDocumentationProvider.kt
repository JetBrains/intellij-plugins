package com.intellij.dts.documentation

import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.DtsProperty
import com.intellij.dts.lang.psi.DtsStatement
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.DocumentationTargetProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

class DtsDocumentationProvider : DocumentationTargetProvider {
    override fun documentationTargets(file: PsiFile, offset: Int): MutableList<out DocumentationTarget> {
        if (file !is DtsFile) return mutableListOf()

        val target = when (val element = findTargetElement(file, offset)) {
            is DtsNode -> DtsNodeDocumentationTarget(element)
            is DtsProperty -> DtsPropertyDocumentationTarget(element)
            else -> return mutableListOf()
        }

        return mutableListOf(target)
    }

    private fun findTargetElement(file: PsiFile, offset: Int): PsiElement? {
        var element = file.findElementAt(offset)
        while (element != null) {
            if (element is DtsStatement) return element

            val reference = element.reference?.resolve()
            if (reference != null) return reference

            element = element.getParent()
        }

        return null
    }
}