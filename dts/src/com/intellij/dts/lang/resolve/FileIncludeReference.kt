package com.intellij.dts.lang.resolve

import com.intellij.dts.lang.psi.FileInclude
import com.intellij.dts.lang.psi.PsiFileInclude
import com.intellij.dts.util.relativeTo
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

class FileIncludeReference private constructor(
    element: PsiElement,
    textRange: TextRange,
    val include: FileInclude,
) : PsiReferenceBase<PsiElement>(element, textRange, true) {
    companion object {
        fun create(element: PsiFileInclude): FileIncludeReference? {
            val range = element.fileIncludeRange ?: return null
            val include = element.fileInclude ?: return null

            return FileIncludeReference(element, range.relativeTo(element.textRange), include)
        }
    }

    override fun resolve(): PsiElement? = include.resolve(element.containingFile)
}