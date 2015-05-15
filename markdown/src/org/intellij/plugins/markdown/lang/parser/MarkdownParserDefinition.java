package org.intellij.plugins.markdown.lang.parser;


import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.intellij.plugins.markdown.lang.lexer.MarkdownLexerAdapter;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownFile;
import org.jetbrains.annotations.NotNull;

public class MarkdownParserDefinition implements ParserDefinition {

  @NotNull
  @Override
  public Lexer createLexer(Project project) {
    return new MarkdownLexerAdapter();
  }

  @NotNull
  @Override
  public PsiParser createParser(Project project) {
    return new MarkdownParserAdapter();
  }

  @NotNull
  @Override
  public IFileElementType getFileNodeType() {
    return MarkdownElementTypes.MARKDOWN_FILE_ELEMENT_TYPE;
  }

  @NotNull
  @Override
  public TokenSet getWhitespaceTokens() {
    return TokenSet.create();
  }

  @NotNull
  @Override
  public TokenSet getCommentTokens() {
    return TokenSet.EMPTY;
  }

  @NotNull
  @Override
  public TokenSet getStringLiteralElements() {
    return TokenSet.EMPTY;
  }

  @NotNull
  @Override
  public PsiElement createElement(ASTNode node) {
    return new ASTWrapperPsiElement(node);
  }

  @NotNull
  @Override
  public PsiFile createFile(FileViewProvider viewProvider) {
    return new MarkdownFile(viewProvider);
  }

  @NotNull
  @Override
  public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
    return SpaceRequirements.MAY;
  }
}
