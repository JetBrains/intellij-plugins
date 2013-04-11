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
    if (node.getElementType() == HbTokenTypes.BLOCK_WRAPPER) {
      return new HbBlockWrapperImpl(node);
    }

    if (node.getElementType() == HbTokenTypes.OPEN_BLOCK_STACHE) {
      return new HbOpenBlockMustacheImpl(node);
    }

    if (node.getElementType() == HbTokenTypes.OPEN_INVERSE_BLOCK_STACHE) {
      return new HbOpenInverseBlockMustacheImpl(node);
    }

    if (node.getElementType() == HbTokenTypes.CLOSE_BLOCK_STACHE) {
      return new HbCloseBlockMustacheImpl(node);
    }

    if (node.getElementType() == HbTokenTypes.MUSTACHE) {
      return new HbSimpleMustacheImpl(node);
    }

    if (node.getElementType() == HbTokenTypes.PATH) {
      return new HbPathImpl(node);
    }

    if (node.getElementType() == HbTokenTypes.DATA) {
      return new HbDataImpl(node);
    }

    if (node.getElementType() == HbTokenTypes.PARAM) {
      return new HbParamImpl(node);
    }

    if (node.getElementType() == HbTokenTypes.PARTIAL_STACHE) {
      return new HbPartialImpl(node);
    }

    if (node.getElementType() == HbTokenTypes.PARTIAL_NAME) {
      return new HbPartialNameImpl(node);
    }

    if (node.getElementType() == HbTokenTypes.SIMPLE_INVERSE) {
      return new HbSimpleInverseImpl(node);
    }

    if (node.getElementType() == HbTokenTypes.STATEMENTS) {
      return new HbStatementsImpl(node);
    }

    if (node.getElementType() == HbTokenTypes.COMMENT) {
      return new HbCommentImpl(node);
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
