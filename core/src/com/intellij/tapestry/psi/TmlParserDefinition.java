package com.intellij.tapestry.psi;

import com.intellij.lang.*;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.PsiFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 *         Date: Jun 18, 2009
 *         Time: 9:25:36 PM
 */
public class TmlParserDefinition implements ParserDefinition {
  @NotNull
  public Lexer createLexer(Project project) {
    return new TmlLexer();
  }

  public IFileElementType getFileNodeType() {
    return TmlElementType.TML_FILE;
  }

  @NotNull
  public TokenSet getWhitespaceTokens() {
    return LanguageParserDefinitions.INSTANCE.forLanguage(Language.findInstance(XMLLanguage.class)).getWhitespaceTokens();
  }

  @NotNull
  public TokenSet getCommentTokens() {
    return LanguageParserDefinitions.INSTANCE.forLanguage(Language.findInstance(XMLLanguage.class)).getCommentTokens();
  }

  @NotNull
  public TokenSet getStringLiteralElements() {
    return TokenSet.EMPTY;
  }

  @NotNull
  public PsiParser createParser(final Project project) {
    return LanguageParserDefinitions.INSTANCE.forLanguage(Language.findInstance(XMLLanguage.class)).createParser(project);
  }

  public PsiFile createFile(FileViewProvider viewProvider) {
    return new TmlFile(viewProvider);
  }

  public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
    final Lexer lexer = createLexer(left.getPsi().getProject());
    return XmlUtil.canStickTokensTogetherByLexerInXml(left, right, lexer, 0);
  }

  @NotNull
  public PsiElement createElement(ASTNode node) {
    throw new IllegalArgumentException("Unknown element: "+node);
  }
}

