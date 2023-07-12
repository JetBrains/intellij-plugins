package com.intellij.dts.lang.parser

import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.DtsTokenSets
import com.intellij.dts.lang.lexer.DtsLexerAdapter
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
    override fun createLexer(project: Project?): Lexer = DtsLexerAdapter()

    override fun createParser(project: Project?): PsiParser = DtsParser()

    override fun getFileNodeType(): IFileElementType = DtsFileStub.Type

    override fun getCommentTokens(): TokenSet = DtsTokenSets.comments

    override fun getStringLiteralElements(): TokenSet = DtsTokenSets.strings

    override fun createElement(node: ASTNode?): PsiElement = DtsTypes.Factory.createElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return when (viewProvider.virtualFile.extension) {
            "overlay" -> DtsFile.Overlay(viewProvider)
            "dtsi" -> DtsFile.Include(viewProvider)
            else -> DtsFile.Source(viewProvider)
        }
    }
}