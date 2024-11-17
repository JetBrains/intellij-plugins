// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.tsr.psi;

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
import com.intellij.tsr.TslLanguage;
import com.intellij.tsr.TslLexer;
import com.intellij.tsr.parser.TslParser;

public final class TslParserDefinition implements ParserDefinition {
  public static final IFileElementType FILE = new IFileElementType(TslLanguage.INSTANCE);

  @Override
  public @NotNull Lexer createLexer(Project project) {
    return new TslLexer();
  }

  @Override
  public @NotNull PsiParser createParser(Project project) {
    return new TslParser();
  }

  @Override
  public @NotNull IFileElementType getFileNodeType() {
    return FILE;
  }

  @Override
  public @NotNull TokenSet getCommentTokens() {
    return TokenSet.EMPTY;
  }

  @Override
  public @NotNull TokenSet getStringLiteralElements() {
    return TslTokenSets.STRINGS;
  }

  @Override
  public @NotNull PsiElement createElement(ASTNode node) {
    return TslTokenTypes.Factory.createElement(node);
  }

  @Override
  public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
    return new TslFile(viewProvider);
  }

  @Override
  public @NotNull SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
    return SpaceRequirements.MAY;
  }
}
