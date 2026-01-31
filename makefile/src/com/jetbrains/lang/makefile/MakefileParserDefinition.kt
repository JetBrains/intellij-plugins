package com.jetbrains.lang.makefile

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.jetbrains.lang.makefile.psi.MakefileTypes

class MakefileParserDefinition : ParserDefinition {
  companion object {
    val FILE: IFileElementType = MakefileStubFileElementType()
  }

  private object TokenSets {
    val WHITE_SPACES: TokenSet = TokenSet.WHITE_SPACE
    val COMMENTS: TokenSet = TokenSet.create(MakefileTypes.COMMENT, MakefileTypes.DOC_COMMENT)
  }

  override fun getFileNodeType() = FILE
  override fun getWhitespaceTokens() = TokenSets.WHITE_SPACES
  override fun getCommentTokens() = TokenSets.COMMENTS
  override fun getStringLiteralElements(): TokenSet = TokenSet.EMPTY

  override fun createFile(viewProvider: FileViewProvider) = MakefileFile(viewProvider)

  override fun createParser(project: Project?) = MakefileParser()
  override fun createLexer(project: Project?) = MakefileLexerAdapter()

  override fun createElement(node: ASTNode?): PsiElement = MakefileTypes.Factory.createElement(node)
}