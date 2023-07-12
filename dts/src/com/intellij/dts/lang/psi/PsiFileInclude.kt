package com.intellij.dts.lang.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

interface FileInclude {
    val offset: Int

    fun resolve(anchor: PsiFile): PsiFile?
}

/**
 * Represents a declaration which imports another file. Used for dts and c
 * preprocessor includes.
 */
interface PsiFileInclude : PsiElement {
    val fileInclude: FileInclude?

    val fileIncludeRange: TextRange?
}