package com.intellij.dts.lang.psi

import com.intellij.dts.lang.DtsAffiliation
import com.intellij.openapi.util.Iconable
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

    interface Node : DtsStatement, Iconable {
        val dtsContent: DtsNodeContent?

        val dtsIsEmpty: Boolean

        val dtsProperties: List<DtsProperty>

        val dtsSubNodes: List<DtsSubNode>
    }

    interface Property : DtsStatement, Iconable {
        val dtsName: String

        val dtsNameElement: PsiElement

        val dtsValues: List<DtsValue>
    }

    interface CompilerDirective : DtsStatement {
        val dtsDirective: PsiElement

        val dtsDirectiveType: IElementType

        val dtsDirectiveArgs: List<PsiElement>
    }
}