package com.intellij.dts.lang.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement

interface DtsNamedElement : PsiNamedElement {
    val dtsNameElement: PsiElement

    val dtsName: String
        get() = dtsNameElement.text
}