package name.kropp.intellij.makefile

import com.intellij.lang.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import name.kropp.intellij.makefile.psi.*

class MakefileParserDefinition : ParserDefinition {
  companion object {
    val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
    val COMMENTS = TokenSet.create(MakefileTypes.COMMENT, MakefileTypes.DOC_COMMENT)

    val FILE = MakefileStubFileElementType()
  }

  override fun getFileNodeType() = FILE
  override fun getWhitespaceTokens() = WHITE_SPACES
  override fun getCommentTokens() = COMMENTS
  override fun getStringLiteralElements(): TokenSet = TokenSet.EMPTY

  override fun createFile(viewProvider: FileViewProvider) = MakefileFile(viewProvider)

  override fun createParser(project: Project?) = MakefileParser()
  override fun createLexer(project: Project?) = MakefileLexerAdapter()

  override fun createElement(node: ASTNode?): PsiElement = MakefileTypes.Factory.createElement(node)
}