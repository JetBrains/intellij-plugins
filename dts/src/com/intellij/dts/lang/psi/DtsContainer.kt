package com.intellij.dts.lang.psi

import com.intellij.psi.PsiElement

interface DtsContainer : PsiElement {
    /**
     * Whether this container is the root container of the file.
     */
    val isDtsRootContainer: Boolean

    val dtsContent: DtsContent?
}