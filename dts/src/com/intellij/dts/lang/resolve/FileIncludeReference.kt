package com.intellij.dts.lang.resolve

import com.intellij.dts.lang.psi.FileInclude
import com.intellij.dts.util.DtsUtil
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

class FileIncludeReference private constructor(
    element: PsiElement,
    textRange: TextRange,
    val path: String
) : PsiReferenceBase<PsiElement>(element, textRange, true) {
    companion object {
        fun create(element: FileInclude): FileIncludeReference? {
            val range = element.fileIncludePathRange ?: return null
            val path = element.fileIncludePath ?: return null

            return FileIncludeReference(element, range, path)
        }
    }

    override fun resolve(): PsiElement? {
        return DtsUtil.resolveRelativeFile(element.containingFile, path)
    }
}