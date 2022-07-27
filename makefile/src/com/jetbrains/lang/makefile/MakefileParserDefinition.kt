package com.jetbrains.lang.makefile

import com.intellij.lang.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import com.jetbrains.lang.makefile.psi.*

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