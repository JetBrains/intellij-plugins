package com.intellij.dts.lang.psi.mixin

import com.intellij.dts.lang.DtsAffiliation
import com.intellij.dts.lang.psi.*
import com.intellij.dts.util.DtsUtil
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

abstract class DtsNodeMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsStatement.Node {
    override val dtsStatementKind: DtsStatementKind
        get() = DtsStatementKind.NODE

    override val dtsName: String
        get() = dtsNameElement.text

    override val dtsAnnotationTarget: PsiElement
        get() = dtsNameElement

    override val dtsContent: DtsNodeContent?
        get() = findChildByClass(DtsNodeContent::class.java)

    override val dtsLabels: List<String>
        get() = findChildrenByType<PsiElement>(DtsTypes.LABEL).map { it.text.trimEnd(':') }

    override fun getTextOffset(): Int = dtsNameElement.textOffset
}

abstract class DtsSubNodeMixin(node: ASTNode) : DtsNodeMixin(node), DtsSubNode {
    override val dtsAffiliation: DtsAffiliation
        get() = DtsAffiliation.NODE

    override val dtsNameElement: PsiElement
        get() = findNotNullChildByType(DtsTypes.NAME)
}

abstract class DtsRootNodeMixin(node: ASTNode) : DtsNodeMixin(node), DtsRootNode {
    override val dtsAffiliation: DtsAffiliation
        get() = DtsAffiliation.ROOT

    override val dtsNameElement: PsiElement
        get() = DtsUtil.children(this).first { it.elementType == DtsTypes.SLASH || it.elementType == DtsTypes.P_HANDLE }
}