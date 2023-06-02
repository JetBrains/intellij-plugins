package com.intellij.dts.lang.psi

import com.intellij.psi.PsiElement

sealed interface DtsNode : DtsContainer, DtsAnnotationTarget {
    interface Root : DtsNode

    interface Sub : DtsNode {
        val dtsName: String

        val dtsNameElement: PsiElement
    }
}