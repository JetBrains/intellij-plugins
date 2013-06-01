package com.jetbrains.lang.dart;

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
import com.jetbrains.lang.dart.psi.DartFile;
import com.jetbrains.lang.dart.psi.impl.DartEmbeddedContentImpl;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Maxim.Mossienko
 * Date: 10/13/11
 * Time: 2:08 PM
 */
public class DartParserDefinition implements ParserDefinition {
  private static final IFileElementType FILE = new IFileElementType(DartLanguage.INSTANCE);

  @NotNull
  @Override
  public Lexer createLexer(Project project) {
    return new DartFlexLexer();
  }

  @Override
  public PsiParser createParser(Project project) {
    return new DartParser();
  }

  @Override
  public IFileElementType getFileNodeType() {
    return DartTokenTypesSets.DART_FILE;
  }

  @NotNull
  @Override
  public TokenSet getWhitespaceTokens() {
    return DartTokenTypesSets.WHITE_SPACES;
  }

  @NotNull
  @Override
  public TokenSet getCommentTokens() {
    return DartTokenTypesSets.COMMENTS;
  }

  @NotNull
  @Override
  public TokenSet getStringLiteralElements() {
    return TokenSet.create(
      DartTokenTypes.RAW_SINGLE_QUOTED_STRING,
      DartTokenTypes.RAW_TRIPLE_QUOTED_STRING,
      DartTokenTypes.OPEN_QUOTE,
      DartTokenTypes.CLOSING_QUOTE,
      DartTokenTypes.REGULAR_STRING_PART
    );
  }

  @NotNull
  @Override
  public PsiElement createElement(ASTNode node) {
    if (node.getElementType() == DartTokenTypesSets.EMBEDDED_CONTENT) {
      return new DartEmbeddedContentImpl(node);
    }
    return DartTokenTypes.Factory.createElement(node);
  }

  @Override
  public PsiFile createFile(FileViewProvider viewProvider) {
    return new DartFile(viewProvider);
  }

  @Override
  public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
    return SpaceRequirements.MAY;
  }
}
