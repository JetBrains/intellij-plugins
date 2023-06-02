package com.intellij.dts.lang.psi.mixin

import com.intellij.dts.lang.psi.*
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

interface IDtsProperty : PsiElement, DtsAnnotationTarget {
    val dtsName: String

    val dtsNameElement: PsiElement

    val dtsValues: List<DtsValue>
}

abstract class DtsPropertyMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsProperty {
    override val dtsAnnotationTarget: PsiElement
        get() = dtsNameElement

    override val dtsNameElement: PsiElement
        get() = findNotNullChildByType<PsiElement>(DtsTypes.NAME)

    override val dtsName: String
        get() = dtsNameElement.text

    override val dtsValues: List<DtsValue>
        get() {
            val content = findChildByClass(DtsPropertyContent::class.java) ?: return emptyList()
            return PsiTreeUtil.getChildrenOfTypeAsList(content, DtsValue::class.java)
        }
}