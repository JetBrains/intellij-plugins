package com.intellij.dts.core.lang.psi.mixin

import com.intellij.dts.core.lang.psi.DtsIncludeStatement
import com.intellij.dts.core.lang.psi.DtsTypes
import com.intellij.dts.core.lang.psi.FileInclude
import com.intellij.dts.core.lang.resolve.FileIncludeReference
import com.intellij.dts.core.lang.resolve.files.DtsIncludeFile
import com.intellij.dts.core.util.relativeTo
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference

abstract class DtsIncludeStatementMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsIncludeStatement {
  private val path: PsiElement?
    get() = findChildByType(DtsTypes.INCLUDE_PATH)

  override val fileInclude: FileInclude?
    get() = path?.let { DtsIncludeFile(it.text.trim('"'), textOffset) }

  override val fileIncludeRange: TextRange?
    get() = path?.textRange?.relativeTo(textRange)

  override fun getReference(): PsiReference? = FileIncludeReference.create(this)
}