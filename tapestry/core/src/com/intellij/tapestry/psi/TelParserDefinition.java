package com.intellij.tapestry.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LanguageUtil;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.tapestry.lang.TelFileType;
import com.intellij.tapestry.psi.impl.TelExpressionHolder;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 */
public class TelParserDefinition implements ParserDefinition {

  @Override
  @NotNull
  public Lexer createLexer(Project project) {
    return new TelLexer();
  }

  @Override
  public @NotNull IFileElementType getFileNodeType() {
    return TelTokenTypes.TEL_FILE;
  }

  @Override
  @NotNull
  public TokenSet getWhitespaceTokens() {
    return TelTokenTypes.WHITESPACES;
  }

  @Override
  @NotNull
  public TokenSet getCommentTokens() {
    return TokenSet.EMPTY;
  }

  @Override
  @NotNull
  public TokenSet getStringLiteralElements() {
    return TelTokenTypes.STRING_LITERALS;
  }

  @Override
  @NotNull
  public PsiParser createParser(final Project project) {
    return new TelParser();
  }

  @Override
  public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
    return new PsiFileBase(viewProvider, TelFileType.INSTANCE.getLanguage()) {
      {
        init(TelTokenTypes.TEL_FILE, TelTokenTypes.TAP5_EL_HOLDER);
      }

      @Override
      @NotNull
      public FileType getFileType() {
        return TelFileType.INSTANCE;
      }
    };
  }

  @Override
  public ParserDefinition.@NotNull SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
    final Lexer lexer = createLexer(left.getPsi().getProject());
    return LanguageUtil.canStickTokensTogetherByLexer(left, right, lexer);
  }

  @Override
  @NotNull
  public PsiElement createElement(ASTNode node) {

    final IElementType elementType = node.getElementType();
    if (elementType instanceof TelCompositeElementType) {
      return ((TelCompositeElementType)elementType).createPsiElement(node);
    }
    if (elementType == TelTokenTypes.TAP5_EL_HOLDER) {
      return new TelExpressionHolder(node);
    }
    throw new AssertionError("Unknown type: " + elementType);
  }

}

