package com.intellij.dts.lang.psi

import com.intellij.psi.PsiElement

sealed interface DtsNode : PsiElement {
    interface Root : DtsNode

    interface Sub : DtsNode
}