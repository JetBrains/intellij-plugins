package com.intellij.dts.lang.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

sealed interface DtsValue : PsiElement {
    val dtsValueRange: TextRange
        get() = textRange

    interface Int : DtsValue {
        fun dtsParse(): kotlin.Int?
    }

    interface String : DtsValue {
        fun dtsParse(): kotlin.String
    }

    interface Untyped : DtsValue
}