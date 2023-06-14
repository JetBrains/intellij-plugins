package com.intellij.dts.lang.psi.mixin

import com.intellij.dts.lang.psi.*
import com.intellij.dts.util.DtsUtil
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

abstract class DtsNodeMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsStatement.Node {
    override val isDtsRootContainer: Boolean = false

    override val dtsAnnotationTarget: PsiElement
        get() = dtsNameElement

    override val dtsContent: DtsContent?
        get() = findChildByClass(DtsNodeContent::class.java)

    override fun getName(): String? {
        return dtsName
    }

    override fun setName(name: String): PsiElement {
        throw UnsupportedOperationException("not implemented")
    }
}

abstract class DtsRootNodeMixin(node: ASTNode) : DtsNodeMixin(node), DtsRootNode {
    override val dtsNameElement: PsiElement
        get() = DtsUtil.children(this).first { it.elementType == DtsTypes.SLASH || it.elementType == DtsTypes.P_HANDLE }
}

abstract class DtsSubNodeMixin(node: ASTNode) : DtsNodeMixin(node), DtsSubNode {
    override val dtsNameElement: PsiElement
        get() = findNotNullChildByType<PsiElement>(DtsTypes.NAME)
}