package com.intellij.dts.lang.psi.mixin

import com.intellij.dts.lang.DtsAffiliation
import com.intellij.dts.lang.DtsTokenSets
import com.intellij.dts.lang.psi.DtsCompilerDirective
import com.intellij.dts.lang.psi.DtsStatementKind
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.dts.util.DtsUtil
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType

abstract class DtsCompilerDirectiveMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsCompilerDirective {
    override val dtsDirectiveType: IElementType
        get() = getDirective().node.elementType

    override val dtsAnnotationTarget: PsiElement
        get() = getDirective()

    override val dtsIsComplete: Boolean
        get() = !PsiTreeUtil.hasErrorElements(this)

    private fun getDirective(): PsiElement {
        return DtsUtil.children(this).first { DtsTokenSets.compilerDirectives.contains(it.elementType) }
    }

    private fun getArgs(): List<PsiElement> {
        return DtsUtil.children(this)
            .dropWhile { !DtsTokenSets.compilerDirectives.contains(it.elementType) }
            .drop(1)
            .toList()
    }

    override val dtsStatementKind: DtsStatementKind
        get() {
            return when(dtsDirectiveType) {
                DtsTypes.DELETE_PROP -> DtsStatementKind.PROPERTY
                DtsTypes.DELETE_NODE -> DtsStatementKind.NODE
                else -> DtsStatementKind.UNKNOWN
            }
        }

    override val dtsAffiliation: DtsAffiliation
        get() {
            return when (dtsDirectiveType) {
                DtsTypes.MEMRESERVE, DtsTypes.V1, DtsTypes.PLUGIN, DtsTypes.OMIT_NODE -> DtsAffiliation.ROOT
                DtsTypes.DELETE_PROP -> DtsAffiliation.NODE
                DtsTypes.DELETE_NODE -> {
                    when (getArgs().firstOrNull()?.elementType) {
                        DtsTypes.NAME -> DtsAffiliation.NODE
                        DtsTypes.P_HANDLE -> DtsAffiliation.ROOT
                        else -> DtsAffiliation.UNKNOWN
                    }
                }
                else -> DtsAffiliation.UNKNOWN
            }
        }
}