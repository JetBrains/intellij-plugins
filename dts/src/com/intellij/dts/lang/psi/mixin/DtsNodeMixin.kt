package com.intellij.dts.lang.psi.mixin

import com.intellij.dts.lang.psi.*
import com.intellij.dts.util.DtsUtil
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

abstract class DtsRootNodeMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsRootNode {
    override val isDtsRootContainer: Boolean = false

    override val dtsContent: DtsContent?
        get() = findChildByClass(DtsNodeContent::class.java)

    override val dtsAnnotationTarget: PsiElement
        get() = DtsUtil.children(this).first { it.elementType == DtsTypes.SLASH || it.elementType == DtsTypes.P_HANDLE }
}

abstract class DtsSubNodeMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsSubNode {
    override val isDtsRootContainer: Boolean = false

    override val dtsContent: DtsContent?
        get() = findChildByClass(DtsNodeContent::class.java)

    override val dtsAnnotationTarget: PsiElement
        get() = dtsNameElement

    override val dtsNameElement: PsiElement
        get() = findNotNullChildByType<PsiElement>(DtsTypes.NAME)

    override val dtsName: String
        get() = dtsNameElement.text
}