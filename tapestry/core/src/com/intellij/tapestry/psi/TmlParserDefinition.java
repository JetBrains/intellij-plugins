package com.intellij.tapestry.psi;

import com.intellij.lang.*;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.lang.xml.XMLParserDefinition;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 */
public class TmlParserDefinition implements ParserDefinition {
  @Override
  @NotNull
  public Lexer createLexer(Project project) {
    return new TmlLexer();
  }

  @Override
  public @NotNull IFileElementType getFileNodeType() {
    return TmlElementType.TML_FILE;
  }

  @Override
  @NotNull
  public TokenSet getWhitespaceTokens() {
    return LanguageParserDefinitions.INSTANCE.forLanguage(Language.findInstance(XMLLanguage.class)).getWhitespaceTokens();
  }

  @Override
  @NotNull
  public TokenSet getCommentTokens() {
    return LanguageParserDefinitions.INSTANCE.forLanguage(Language.findInstance(XMLLanguage.class)).getCommentTokens();
  }

  @Override
  @NotNull
  public TokenSet getStringLiteralElements() {
    return TokenSet.EMPTY;
  }

  @Override
  @NotNull
  public PsiParser createParser(final Project project) {
    return LanguageParserDefinitions.INSTANCE.forLanguage(Language.findInstance(XMLLanguage.class)).createParser(project);
  }

  @Override
  public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
    return new TmlFile(viewProvider);
  }

  @Override
  public @NotNull SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
    return XMLParserDefinition.canStickTokensTogether(left, right);
  }

  @Override
  @NotNull
  public PsiElement createElement(ASTNode node) {
    throw new IllegalArgumentException("Unknown element: "+node);
  }
}

