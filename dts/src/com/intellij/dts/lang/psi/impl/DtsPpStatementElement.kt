package com.intellij.dts.lang.psi.impl

import com.intellij.dts.lang.psi.DtsIncludeStatement
import com.intellij.dts.lang.psi.FileInclude
import com.intellij.dts.lang.resolve.FileIncludeReference
import com.intellij.dts.lang.resolve.files.DtsIncludeFile
import com.intellij.dts.pp.lang.parser.PpAdHocParser
import com.intellij.dts.pp.lang.psi.PpIncludeStatement
import com.intellij.dts.pp.lang.psi.PpStatementPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference
import com.intellij.util.asSafely

class DtsPpStatementElement(node: ASTNode, parser: PpAdHocParser) : PpStatementPsiElement(node, parser), DtsIncludeStatement {
  private val includeStatement = statement.asSafely<PpIncludeStatement>()

  override val fileIncludeRange: TextRange?
    get() = includeStatement?.headerName?.range

  override val fileInclude: FileInclude?
    get() = includeStatement?.headerName?.let { name -> DtsIncludeFile(name.text.trim('"', '<', '>').toString(), textOffset) }

  override fun getReference(): PsiReference? = FileIncludeReference.create(this)
}