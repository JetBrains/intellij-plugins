package com.intellij.dts.lang.psi

import com.intellij.dts.lang.DtsAffiliation
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType

enum class DtsStatementKind {
    NODE,
    PROPERTY,
    UNKNOWN;

    fun isNode(): Boolean = this == NODE

    fun isProperty(): Boolean = this == PROPERTY

    fun isUnknown(): Boolean = this == UNKNOWN
}

sealed interface DtsStatement : PsiElement {
    val dtsAnnotationTarget: PsiElement

    val dtsAffiliation: DtsAffiliation

    val dtsStatementKind: DtsStatementKind

    /**
     * If a parser rule has the recoverWhile property set, it could be the case
     * that errors are not inner nodes if the generated PsiElement. Therefore,
     * PsiTreeUtil.hasErrorElements is not sufficient to check if a PsiElement
     * is complete. In this case only relevant to check if a node has a matching
     * brace.
     */
    val dtsIsComplete: Boolean

    interface Node : DtsStatement {
        val dtsName: String

        val dtsNameElement: PsiElement

        val dtsContent: DtsNodeContent?

        val dtsLabels: List<String>

        val dtsIsEmpty: Boolean
    }

    interface Property : DtsStatement {
        val dtsName: String

        val dtsNameElement: PsiElement

        val dtsValues: List<DtsValue>
    }

    interface CompilerDirective : DtsStatement {
        val dtsDirectiveType: IElementType
    }
}