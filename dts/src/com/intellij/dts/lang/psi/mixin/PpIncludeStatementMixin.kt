package com.intellij.dts.lang.psi.mixin

import com.intellij.dts.lang.DtsPpTokenTypes
import com.intellij.dts.lang.psi.DtsPpIncludeStatement
import com.intellij.dts.lang.psi.FileInclude
import com.intellij.dts.lang.resolve.FileIncludeReference
import com.intellij.dts.lang.resolve.files.DtsIncludeFile
import com.intellij.dts.util.trim
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference

private val trimPathChars = arrayOf('"', '<', '>').toCharArray()

abstract class PpIncludeStatementMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsPpIncludeStatement {
  private val path: PsiElement?
    get() = findChildByType(DtsPpTokenTypes.includePath)

  override val fileInclude: FileInclude?
    get() = path?.let { DtsIncludeFile(it.text.trim(*trimPathChars), textOffset) }

  override val fileIncludeRange: TextRange?
    get() = path?.let { it.textRange.trim(it.text, *trimPathChars) }

  override fun getReference(): PsiReference? = FileIncludeReference.create(this)
}