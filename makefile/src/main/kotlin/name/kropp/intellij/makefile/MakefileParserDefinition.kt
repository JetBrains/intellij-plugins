package name.kropp.intellij.makefile

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet
import name.kropp.intellij.makefile.psi.MakefileTypes

class MakefileParserDefinition : ParserDefinition {
  companion object {
    val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
    val COMMENTS = TokenSet.create(MakefileTypes.COMMENT, MakefileTypes.DOC_COMMENT)

    val FILE = MakefileStubFileElementType()
  }

  override fun getFileNodeType() = FILE
  override fun getWhitespaceTokens() = WHITE_SPACES
  override fun getCommentTokens() = COMMENTS
  override fun getStringLiteralElements() = TokenSet.EMPTY

  override fun spaceExistanceTypeBetweenTokens(left: ASTNode?, right: ASTNode?) = ParserDefinition.SpaceRequirements.MAY

  override fun createFile(viewProvider: FileViewProvider) = MakefileFile(viewProvider)

  override fun createParser(project: Project?) = MakefileParser()
  override fun createLexer(project: Project?) = MakefileLexerAdapter()

  override fun createElement(node: ASTNode?) = MakefileTypes.Factory.createElement(node)
}