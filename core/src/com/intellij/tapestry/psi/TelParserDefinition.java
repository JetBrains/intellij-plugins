package com.intellij.tapestry.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.*;
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
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 *         Date: Jun 22, 2009
 *         Time: 9:16:55 PM
 */
public class TelParserDefinition implements ParserDefinition {

  @NotNull
  public Lexer createLexer(Project project) {
    return new TelLexer();
  }

  public IFileElementType getFileNodeType() {
    return TelElementTypes.TEL_FILE;
  }

  @NotNull
  public TokenSet getWhitespaceTokens() {
    return TokenSet.EMPTY;
  }

  @NotNull
  public TokenSet getCommentTokens() {
    return TokenSet.EMPTY;
  }

  @NotNull
  public TokenSet getStringLiteralElements() {
    return TokenSet.EMPTY;
  }

  @NotNull
  public PsiParser createParser(final Project project) {
    return new TelParser();
  }

  public PsiFile createFile(FileViewProvider viewProvider) {
    return new PsiFileBase(viewProvider, TelFileType.INSTANCE.getLanguage()) {
      {
        init(TelElementTypes.TEL_FILE, TelElementTypes.TAP5_EL_HOLDER);
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

    final IElementType type = node.getElementType();
    if (type instanceof TelElementType) {
      return ((TelElementType)type).createPsiElement(node);
    }
    throw new AssertionError("Unknown type: " + type);
  }

}

