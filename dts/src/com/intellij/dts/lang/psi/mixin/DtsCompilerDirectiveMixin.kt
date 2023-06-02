package com.intellij.dts.lang.psi.mixin

import com.intellij.dts.lang.DtsTokenSets
import com.intellij.dts.lang.psi.DtsAnnotationTarget
import com.intellij.dts.lang.psi.DtsCompilerDirective
import com.intellij.dts.util.DtsUtil
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType

interface IDtsCompilerDirective : DtsAnnotationTarget {
    val dtsDirectiveType: IElementType
}

abstract class DtsCompilerDirectiveMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsCompilerDirective {
    override val dtsDirectiveType: IElementType
        get() = getDirective().node.elementType

    override val dtsAnnotationTarget: PsiElement
        get() = getDirective()

    private fun getDirective(): PsiElement {
        return DtsUtil.children(this).first { DtsTokenSets.compilerDirectives.contains(it.elementType) }
    }
}