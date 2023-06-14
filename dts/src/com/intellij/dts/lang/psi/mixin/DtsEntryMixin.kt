package com.intellij.dts.lang.psi.mixin

import com.intellij.dts.lang.psi.DtsEntry
import com.intellij.dts.lang.psi.DtsStatement
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.dts.util.DtsUtil
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

interface IDtsEntry : PsiElement {
    val dtsStatement: DtsStatement

    val hasDtsSemicolon: Boolean
}

abstract class DtsEntryMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsEntry {
    override val dtsStatement: DtsStatement
        get() = findNotNullChildByClass(DtsStatement::class.java)

    override val hasDtsSemicolon: Boolean
        get() {
            val lastChild = DtsUtil.children(this, forward = false).firstOrNull() ?: return false
            return lastChild.elementType == DtsTypes.SEMICOLON
        }
}