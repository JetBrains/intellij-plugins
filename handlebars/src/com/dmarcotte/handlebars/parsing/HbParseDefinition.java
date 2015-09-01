package com.dmarcotte.handlebars.parsing;

import com.dmarcotte.handlebars.psi.HbPsiFile;
import com.dmarcotte.handlebars.psi.impl.*;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

public class HbParseDefinition implements ParserDefinition {
  @NotNull
  public Lexer createLexer(Project project) {
    return new HbLexer();
  }

  public PsiParser createParser(Project project) {
    return new HbParser();
  }

  public IFileElementType getFileNodeType() {
    return HbTokenTypes.FILE;
  }

  @NotNull
  public TokenSet getWhitespaceTokens() {
    return HbTokenTypes.WHITESPACES;
  }

  @NotNull
  public TokenSet getCommentTokens() {
    return HbTokenTypes.COMMENTS;
  }

  @NotNull
  public TokenSet getStringLiteralElements() {
    return HbTokenTypes.STRING_LITERALS;
  }

  @NotNull
  public PsiElement createElement(ASTNode node) {
    final IElementType elementType = node.getElementType();
    if (elementType == HbTokenTypes.BLOCK_WRAPPER) {
      return new HbBlockWrapperImpl(node);
    }

    if (elementType == HbTokenTypes.OPEN_BLOCK_STACHE) {
      return new HbOpenBlockMustacheImpl(node);
    }

    if (elementType == HbTokenTypes.OPEN_INVERSE_BLOCK_STACHE) {
      return new HbOpenInverseBlockMustacheImpl(node);
    }

    if (elementType == HbTokenTypes.CLOSE_BLOCK_STACHE) {
      return new HbCloseBlockMustacheImpl(node);
    }

    if (elementType == HbTokenTypes.MUSTACHE) {
      return new HbSimpleMustacheImpl(node);
    }

    if (elementType == HbTokenTypes.MUSTACHE_NAME) {
      return new HbMustacheNameImpl(node);
    }

    if (elementType == HbTokenTypes.PATH) {
      return new HbPathImpl(node);
    }

    if (elementType == HbTokenTypes.DATA) {
      return new HbDataImpl(node);
    }

    if (elementType == HbTokenTypes.PARAM) {
      return new HbParamImpl(node);
    }

    if (elementType == HbTokenTypes.PARTIAL_STACHE) {
      return new HbPartialImpl(node);
    }

    if (elementType == HbTokenTypes.PARTIAL_NAME) {
      return new HbPartialNameImpl(node);
    }

    if (elementType == HbTokenTypes.SIMPLE_INVERSE) {
      return new HbSimpleInverseImpl(node);
    }

    if (elementType == HbTokenTypes.STATEMENTS) {
      return new HbStatementsImpl(node);
    }

    if (elementType == HbTokenTypes.COMMENT) {
      return new HbCommentImpl(node);
    }

    if (elementType == HbTokenTypes.HASH) {
      return new HbHashImpl(node);
    }

    return new HbPsiElementImpl(node);
  }

  public PsiFile createFile(FileViewProvider viewProvider) {
    return new HbPsiFile(viewProvider);
  }

  public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
    return SpaceRequirements.MAY;
  }
}
