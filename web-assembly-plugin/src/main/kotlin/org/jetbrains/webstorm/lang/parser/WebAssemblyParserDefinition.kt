package org.jetbrains.webstorm.lang.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.ParserDefinition.SpaceRequirements
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import org.jetbrains.webstorm.lang.WebAssemblyLanguage
import org.jetbrains.webstorm.lang.lexer.WebAssemblyLexer
import org.jetbrains.webstorm.lang.psi.WebAssemblyTypes
import org.jetbrains.webstorm.lang.psi.WebAssemblyFile


class WebAssemblyParserDefinition : ParserDefinition {
    override fun createLexer(project: Project?): Lexer = WebAssemblyLexer()

    override fun getWhitespaceTokens(): TokenSet = WHITE_SPACES

    override fun getCommentTokens(): TokenSet = COMMENTS

    override fun getStringLiteralElements(): TokenSet = TokenSet.EMPTY

    override fun createParser(project: Project?): PsiParser = WebAssemblyParser()

    override fun getFileNodeType(): IFileElementType = FILE

    override fun createFile(viewProvider: FileViewProvider): PsiFile = WebAssemblyFile(viewProvider)
    
    override fun spaceExistenceTypeBetweenTokens(left: ASTNode?, right: ASTNode?): SpaceRequirements =
        SpaceRequirements.MAY

    override fun createElement(node: ASTNode?): PsiElement = WebAssemblyTypes.Factory.createElement(node)

    companion object {
        val WHITE_SPACES: TokenSet = TokenSet.create(TokenType.WHITE_SPACE)
        val COMMENTS: TokenSet     = TokenSet.create(WebAssemblyTypes.LINE_COMMENT, WebAssemblyTypes.BLOCK_COMMENT)
        val FILE: IFileElementType = IFileElementType(WebAssemblyLanguage)
    }
}