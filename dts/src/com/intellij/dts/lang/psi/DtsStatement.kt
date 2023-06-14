package com.intellij.dts.lang.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType

sealed interface DtsStatement : PsiElement {
    val dtsAnnotationTarget: PsiElement

    interface Node : DtsStatement, DtsContainer, DtsNamedElement

    interface Property : DtsStatement, DtsNamedElement {
        val dtsValues: List<DtsValue>
    }

    interface CompilerDirective : DtsStatement {
        val dtsDirectiveType: IElementType
    }
}