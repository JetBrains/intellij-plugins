package com.jetbrains.plugins.jade;

import com.intellij.application.options.CodeStyle;
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
import com.jetbrains.plugins.jade.lexer.JadeLexer;
import com.jetbrains.plugins.jade.parser.JadeParser;
import com.jetbrains.plugins.jade.psi.JadeFileImpl;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;
import com.jetbrains.plugins.jade.psi.impl.JadeMixinDeclarationImpl;
import com.jetbrains.plugins.jade.psi.stubs.JadeStubElementTypes;
import org.jetbrains.annotations.NotNull;

public final class JadeParserDefinition implements ParserDefinition {

  @Override
  public @NotNull Lexer createLexer(final Project project) {
    return new JadeLexer(CodeStyle.getSettings(project));
  }

  @Override
  public @NotNull PsiParser createParser(final Project project) {
    return new JadeParser(CodeStyle.getSettings(project));
  }

  @Override
  public @NotNull IFileElementType getFileNodeType() {
    return JadeStubElementTypes.JADE_FILE;
  }

  @Override
  public @NotNull TokenSet getCommentTokens() {
    return TokenSet.EMPTY;
  }

  @Override
  public @NotNull TokenSet getStringLiteralElements() {
    return JadeTokenTypes.STRING_LITERALS;
  }

  @Override
  public @NotNull PsiElement createElement(final ASTNode node) {
    if (node.getElementType() == JadeStubElementTypes.MIXIN_DECLARATION) {
      return new JadeMixinDeclarationImpl(node);
    }
    return new ASTWrapperPsiElement(node);
  }

  @Override
  public @NotNull PsiFile createFile(final @NotNull FileViewProvider viewProvider) {
    return new JadeFileImpl(viewProvider);
  }

  @Override
  public @NotNull SpaceRequirements spaceExistenceTypeBetweenTokens(final ASTNode left, final ASTNode right) {
    return ParserDefinition.SpaceRequirements.MAY;
  }
}
