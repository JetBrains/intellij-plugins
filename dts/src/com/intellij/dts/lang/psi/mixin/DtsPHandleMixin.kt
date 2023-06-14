package com.intellij.dts.lang.psi.mixin

import com.intellij.dts.lang.psi.DtsPHandle
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

interface IDtsPHandle : PsiElement {
    val dtsPHandleLabel: PsiElement?

    val dtsPHandlePath: PsiElement?
}

abstract class DtsPHandleMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsPHandle {
    override val dtsPHandleLabel: PsiElement?
        get() = findChildByType(DtsTypes.NAME)

    override val dtsPHandlePath: PsiElement?
        get() = findChildByType(DtsTypes.PATH)
}