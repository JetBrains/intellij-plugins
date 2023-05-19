package com.intellij.dts.lang.psi

import com.intellij.psi.PsiElement

sealed interface DtsArray : PsiElement {
    interface Cell : DtsArray

    interface Byte : DtsArray
}