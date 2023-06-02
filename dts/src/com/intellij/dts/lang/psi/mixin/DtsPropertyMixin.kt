package com.intellij.dts.lang.psi.mixin

import com.intellij.dts.lang.psi.DtsAnnotationTarget
import com.intellij.dts.lang.psi.DtsProperty
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

interface IDtsProperty : PsiElement, DtsAnnotationTarget {
    val dtsName: String

    val dtsNameElement: PsiElement
}

abstract class DtsPropertyMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsProperty {
    override val dtsAnnotationTarget: PsiElement
        get() = dtsNameElement

    override val dtsNameElement: PsiElement
        get() = findNotNullChildByType<PsiElement>(DtsTypes.NAME)

    override val dtsName: String
        get() = dtsNameElement.text
}