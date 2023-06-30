package com.intellij.dts.lang.psi.mixin

import com.intellij.dts.lang.psi.DtsPpHeader
import com.intellij.dts.lang.psi.DtsPpIncludeStatement
import com.intellij.dts.lang.resolve.FileIncludeReference
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference

abstract class PpIncludeStatementMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsPpIncludeStatement {
    private val header: DtsPpHeader?
        get() = findChildByClass(DtsPpHeader::class.java)

    override val fileIncludePath: String?
        get() = header?.let {
            val headerText = it.text

            if (headerText.length > 2) {
                headerText.substring(1, headerText.length - 1)
            } else {
                ""
            }
        }

    override val fileIncludePathRange: TextRange?
        get() = header?.let {
            val headerRange = it.textRangeInParent

            if (headerRange.length > 2) {
                headerRange.grown(-2).shiftRight(1)
            } else {
                headerRange
            }
        }

    override fun getReference(): PsiReference? = FileIncludeReference.create(this)
}