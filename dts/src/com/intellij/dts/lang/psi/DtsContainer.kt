package com.intellij.dts.lang.psi

import com.intellij.psi.PsiElement

interface DtsContainer : PsiElement {
    val isDtsRootContainer: Boolean
}