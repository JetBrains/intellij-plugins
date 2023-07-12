package com.intellij.dts.lang.psi.mixin

import com.intellij.dts.lang.psi.DtsIncludeStatement
import com.intellij.dts.lang.psi.DtsString
import com.intellij.dts.lang.psi.FileInclude
import com.intellij.dts.lang.resolve.FileIncludeReference
import com.intellij.dts.lang.resolve.files.DtsIncludeFile
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference

abstract class DtsIncludeStatementMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsIncludeStatement {
    private val pathString: DtsString?
        get() = findChildByClass(DtsString::class.java)

    override val fileInclude: FileInclude?
        get() = pathString?.let { DtsIncludeFile(it.dtsParse(), textOffset) }

    override val fileIncludeRange: TextRange?
        get() = pathString?.dtsValueRange

    override fun getReference(): PsiReference? = FileIncludeReference.create(this)
}