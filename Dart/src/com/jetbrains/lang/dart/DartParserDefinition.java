// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart;

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
import com.jetbrains.lang.dart.lexer.DartLexer;
import com.jetbrains.lang.dart.psi.DartFile;
import com.jetbrains.lang.dart.psi.impl.DartDocCommentImpl;
import com.jetbrains.lang.dart.psi.impl.DartEmbeddedContentImpl;
import org.jetbrains.annotations.NotNull;

public final class DartParserDefinition implements ParserDefinition {
  public static final IFileElementType DART_FILE = new IFileElementType("DARTFILE", DartLanguage.INSTANCE);

  @Override
  public @NotNull Lexer createLexer(Project project) {
    return new DartLexer();
  }

  @Override
  public @NotNull PsiParser createParser(Project project) {
    return new DartParser();
  }

  @Override
  public @NotNull IFileElementType getFileNodeType() {
    return DART_FILE;
  }

  @Override
  public @NotNull TokenSet getCommentTokens() {
    return DartTokenTypesSets.COMMENTS;
  }

  @Override
  public @NotNull TokenSet getStringLiteralElements() {
    return TokenSet.create(
      DartTokenTypes.RAW_SINGLE_QUOTED_STRING,
      DartTokenTypes.RAW_TRIPLE_QUOTED_STRING,
      DartTokenTypes.OPEN_QUOTE,
      DartTokenTypes.CLOSING_QUOTE,
      DartTokenTypes.REGULAR_STRING_PART
    );
  }

  @Override
  public @NotNull PsiElement createElement(ASTNode node) {
    final IElementType type = node.getElementType();

    if (type == DartTokenTypesSets.EMBEDDED_CONTENT) return new DartEmbeddedContentImpl(node);
    if (type == DartTokenTypesSets.MULTI_LINE_DOC_COMMENT) return new DartDocCommentImpl(node);

    return DartTokenTypes.Factory.createElement(node);
  }

  @Override
  public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
    return new DartFile(viewProvider);
  }

  @Override
  public @NotNull SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
    return SpaceRequirements.MAY;
  }
}
