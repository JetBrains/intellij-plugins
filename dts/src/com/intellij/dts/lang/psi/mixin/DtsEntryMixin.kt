package com.intellij.dts.lang.psi.mixin

import com.intellij.dts.lang.psi.DtsEntry
import com.intellij.dts.lang.psi.DtsAnnotationTarget
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.dts.util.DtsUtil
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

interface IDtsEntry : PsiElement, DtsAnnotationTarget {
    val dtsStatement: PsiElement

    val hasDtsSemicolon: Boolean
}

abstract class DtsEntryMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsEntry {
    override val dtsStatement: PsiElement
        get() = DtsUtil.children(this).first()

    override val dtsAnnotationTarget: PsiElement
        get() {
            val statement = dtsStatement

            return if (statement is DtsAnnotationTarget) {
                statement.dtsAnnotationTarget
            } else {
                statement
            }
        }

    override val hasDtsSemicolon: Boolean
        get() {
            val lastChild = DtsUtil.children(this, forward = false).firstOrNull() ?: return false
            return lastChild.elementType == DtsTypes.SEMICOLON
        }
}