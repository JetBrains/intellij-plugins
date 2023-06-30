package com.intellij.dts.lang.psi.mixin

import com.intellij.dts.lang.psi.DtsIncludeStatement
import com.intellij.dts.lang.psi.DtsString
import com.intellij.dts.lang.resolve.FileIncludeReference
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference

abstract class DtsIncludeStatementMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsIncludeStatement {
    private val pathString: DtsString?
        get() = findChildByClass(DtsString::class.java)

    override val fileIncludePath: String?
        get() = pathString?.dtsParse()

    override val fileIncludePathRange: TextRange?
        get() = pathString?.let {
            val stringRange = it.textRangeInParent

            if (stringRange.length > 2) {
                stringRange.grown(-2).shiftRight(1)
            } else {
                stringRange
            }
        }

    override fun getReference(): PsiReference? = FileIncludeReference.create(this)
}