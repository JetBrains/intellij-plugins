// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.plugins.drools.DroolsLanguage;
import com.intellij.plugins.drools.lang.lexer.*;
import com.intellij.plugins.drools.lang.psi.DroolsCompositeBlockExpressionElement;
import com.intellij.plugins.drools.lang.psi.DroolsCompositeJavaStatementElement;
import com.intellij.plugins.drools.lang.psi.DroolsFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

public final class DroolsParserDefinition implements ParserDefinition {
  private static final IFileElementType DROOLS_FILE = new IFileElementType("DROOLS_FILE", DroolsLanguage.INSTANCE);

  @Override
  public @NotNull Lexer createLexer(Project project) {
    return new DroolsLexer();
  }

  @Override
  public @NotNull PsiParser createParser(Project project) {
    return new DroolsParser();
  }

  @Override
  public @NotNull IFileElementType getFileNodeType() {
    return DROOLS_FILE;
  }

  @Override
  public @NotNull TokenSet getCommentTokens() {
    return DroolsTokenTypeSets.COMMENTS;
  }

  @Override
  public @NotNull TokenSet getStringLiteralElements() {
    return DroolsTokenTypeSets.STRINGS;
  }

  @Override
  public @NotNull PsiElement createElement(ASTNode node) {
    if (node.getElementType() instanceof DroolsJavaStatementLazyParseableElementType) {
      return new DroolsCompositeJavaStatementElement(node);
    }
    if (node.getElementType() instanceof DroolsBlockExpressionsLazyParseableElementType) {
      return new DroolsCompositeBlockExpressionElement(node);
    }
    return DroolsTokenTypes.Factory.createElement(node);
  }

  @Override
  public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
    return new DroolsFile(viewProvider);
  }

  @Override
  public @NotNull SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
    return SpaceRequirements.MAY;
  }
}
