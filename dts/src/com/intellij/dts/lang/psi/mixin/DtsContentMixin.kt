package com.intellij.dts.lang.psi.mixin

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.dts.lang.psi.DtsContainer
import com.intellij.dts.lang.psi.DtsContent

interface IDtsContent : PsiElement {
    val dtsContainer: DtsContainer
}

abstract class DtsContentMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsContent {
    override val dtsContainer: DtsContainer
        get() = parent as DtsContainer
}
