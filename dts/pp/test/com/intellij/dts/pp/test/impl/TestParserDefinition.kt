package com.intellij.dts.pp.test.impl

import com.intellij.dts.pp.test.impl.psi.TestTypes
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

class TestParserDefinition : ParserDefinition {
  override fun createLexer(project: Project?): Lexer = TestParserLexerAdapter()

  override fun createParser(project: Project?): PsiParser = TestParser()

  override fun getFileNodeType(): IFileElementType = IFileElementType(TestLanguage)

  override fun getCommentTokens(): TokenSet = TokenSet.create(TestTypes.PP_COMMENT, TestTypes.COMMENT, TestTypes.PP_INACTIVE)

  override fun getStringLiteralElements(): TokenSet = TokenSet.EMPTY

  override fun createElement(node: ASTNode?): PsiElement = TestPpTokenTypes.createElement(node, TestTypes.Factory::createElement)

  override fun createFile(viewProvider: FileViewProvider): PsiFile = TestFile(viewProvider)
}