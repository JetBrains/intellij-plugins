package com.intellij.dts.lang.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

// used for dts includes and cpp includes
interface FileInclude : PsiElement {
    val fileIncludePath: String?

    val fileIncludePathRange: TextRange?
}