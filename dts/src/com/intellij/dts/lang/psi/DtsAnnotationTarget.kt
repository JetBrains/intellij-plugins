package com.intellij.dts.lang.psi

import com.intellij.psi.PsiElement

/**
 * Implement by psi elements that provide a custom annotation target. For
 * instance this is implement by dts nodes to only apply annotation to their
 * name.
 */
interface DtsAnnotationTarget {
    val dtsAnnotationTarget: PsiElement
}