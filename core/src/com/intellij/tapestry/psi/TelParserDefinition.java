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

  @NotNull
  public Lexer createLexer(Project project) {
    return new TelLexer();
  }

  public IFileElementType getFileNodeType() {
    return TelTokenTypes.TEL_FILE;
  }

  @NotNull
  public TokenSet getWhitespaceTokens() {
    return TelTokenTypes.WHITESPACES;
  }

  @NotNull
  public TokenSet getCommentTokens() {
    return TokenSet.EMPTY;
  }

  @NotNull
  public TokenSet getStringLiteralElements() {
    return TelTokenTypes.STRING_LITERALS;
  }

  @NotNull
  public PsiParser createParser(final Project project) {
    return new TelParser();
  }

  public PsiFile createFile(FileViewProvider viewProvider) {
    return new PsiFileBase(viewProvider, TelFileType.INSTANCE.getLanguage()) {
      {
        init(TelTokenTypes.TEL_FILE, TelTokenTypes.TAP5_EL_HOLDER);
      }

      @NotNull
      public FileType getFileType() {
        return TelFileType.INSTANCE;
      }
    };
  }

  public ParserDefinition.SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
    final Lexer lexer = createLexer(left.getPsi().getProject());
    return LanguageUtil.canStickTokensTogetherByLexer(left, right, lexer);
  }

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

