package com.intellij.dts.lang.psi.mixin

import com.intellij.dts.lang.psi.DtsPpHeader
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.dts.util.DtsUtil
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

interface IPpHeader : PsiElement {
    val ppPath: String

    val ppPathRange: TextRange
}

abstract class PpHeaderMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsPpHeader {
    private val path: PsiElement?
        get() = findChildByType(DtsTypes.PP_PATH)

    override val ppPath: String
        get() = path?.text ?: ""

    override val ppPathRange: TextRange
        get() = path?.textRange ?: DtsUtil.trimEnds(textRange)
}