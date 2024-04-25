package com.intellij.dts.lang.parser

import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.DtsPpTokenTypes
import com.intellij.dts.lang.DtsTokenSets
import com.intellij.dts.lang.lexer.DtsParserLexerAdapter
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.dts.lang.stubs.DtsFileStub
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

open class DtsParserDefinition : ParserDefinition {
  override fun createLexer(project: Project?): Lexer = DtsParserLexerAdapter()

  override fun createParser(project: Project?): PsiParser = DtsParser()

  override fun getFileNodeType(): IFileElementType = DtsFileStub.Type

  override fun getCommentTokens(): TokenSet = TokenSet.create(*DtsTokenSets.comments.types, DtsTypes.PP_INACTIVE)

  override fun getStringLiteralElements(): TokenSet = DtsTokenSets.strings

  override fun createElement(node: ASTNode?): PsiElement = DtsPpTokenTypes.createElement(node, DtsTypes.Factory::createElement)

  override fun createFile(viewProvider: FileViewProvider): PsiFile {
    return when (viewProvider.virtualFile.extension) {
      "overlay" -> DtsFile.Overlay(viewProvider)
      "dtsi" -> DtsFile.Include(viewProvider)
      else -> DtsFile.Source(viewProvider)
    }
  }
}