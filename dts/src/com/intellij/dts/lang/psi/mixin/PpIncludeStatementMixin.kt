package com.intellij.dts.lang.psi.mixin

import com.intellij.dts.lang.psi.DtsPpHeader
import com.intellij.dts.lang.psi.DtsPpIncludeStatement
import com.intellij.dts.lang.psi.FileInclude
import com.intellij.dts.lang.resolve.FileIncludeReference
import com.intellij.dts.lang.resolve.files.DtsIncludeFile
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference

abstract class PpIncludeStatementMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsPpIncludeStatement {
    private val header: DtsPpHeader?
        get() = findChildByClass(DtsPpHeader::class.java)

    override val fileInclude: FileInclude?
        get() = header?.let { DtsIncludeFile(it.ppPath, textOffset) }

    override val fileIncludeRange: TextRange?
        get() = header?.ppPathRange

    override fun getReference(): PsiReference? = FileIncludeReference.create(this)
}