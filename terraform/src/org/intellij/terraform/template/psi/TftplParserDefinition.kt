// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.template.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import org.intellij.terraform.hil.HILElementType
import org.intellij.terraform.hil.HILElementTypes
import org.intellij.terraform.hil.HILTokenType
import org.intellij.terraform.hil.psi.TerraformTemplateLanguage
import org.intellij.terraform.hil.psi.TerraformTemplateTokenTypes.DATA_LANGUAGE_TOKEN_UNPARSED
import org.intellij.terraform.template.lexer.TerraformTemplateLexer

internal class TftplParserDefinition : ParserDefinition {
  private val TFTPL_FILE: IFileElementType = IFileElementType("TFTPL_FILE", TerraformTemplateLanguage)

  override fun createLexer(project: Project?): Lexer = TerraformTemplateLexer()

  override fun createParser(project: Project?): PsiParser = TerraformTemplateHilBasedParser()

  override fun getFileNodeType(): IFileElementType = TFTPL_FILE

  override fun getCommentTokens(): TokenSet = TokenSet.EMPTY

  override fun getStringLiteralElements(): TokenSet = TokenSet.EMPTY

  override fun createFile(viewProvider: FileViewProvider): PsiFile = TftplFile(viewProvider)

  override fun createElement(node: ASTNode): PsiElement {
    return when (node.elementType) {
      DATA_LANGUAGE_TOKEN_UNPARSED -> TftplDataLanguageSegmentImpl(node)
      is HILElementType -> HILElementTypes.Factory.createElement(node)
      is HILTokenType -> HILElementTypes.Factory.createElement(node)
      else -> ASTWrapperPsiElement(node)
    }
  }
}