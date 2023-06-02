package com.intellij.dts.lang.parser

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
import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.DtsLanguage
import com.intellij.dts.lang.DtsTokenSets
import com.intellij.dts.lang.lexer.DtsLexerAdapter
import com.intellij.dts.lang.psi.DtsTypes

private val file = IFileElementType(DtsLanguage)

open class DtsParserDefinition : ParserDefinition {
    override fun createLexer(project: Project?): Lexer = DtsLexerAdapter()

    override fun createParser(project: Project?): PsiParser = DtsParser()

    override fun getFileNodeType(): IFileElementType = file

    override fun getCommentTokens(): TokenSet = DtsTokenSets.comments

    override fun getStringLiteralElements(): TokenSet = DtsTokenSets.strings

    override fun createElement(node: ASTNode?): PsiElement = DtsTypes.Factory.createElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return if (viewProvider.virtualFile.extension == "dtsi") {
            DtsFile.Include(viewProvider)
        } else {
            DtsFile.Source(viewProvider)
        }
    }
}