// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.psi;

import com.intellij.embedding.EmbeddedLazyParseableElementType;
import com.intellij.embedding.MasqueradingPsiBuilderAdapter;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiBuilder;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.jetbrains.plugins.jade.lexer.JadeSimpleInterpolationLexer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The one that redefines the parsing routines entirely
 */
abstract class JadeLazyParseableElementType extends EmbeddedLazyParseableElementType {
  JadeLazyParseableElementType(@NotNull @NonNls String debugName, @Nullable Language language) {
    super(debugName, language);
  }

  protected abstract void parseIntoBuilder(@NotNull PsiBuilder builder);

  @Override
  public PsiBuilder getBuilder(ASTNode chameleon, Project project, ParserDefinition parserDefinition, Lexer lexer, CharSequence chars) {
    return new MasqueradingPsiBuilderAdapter(project, parserDefinition, new JadeSimpleInterpolationLexer(lexer), chameleon, chars);
  }

  @Override
  public Lexer createLexer(@NotNull ASTNode chameleon, @NotNull Project project) {
    final ParserDefinition parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(getLanguage());
    return parserDefinition.createLexer(project);
  }

  @Override
  public ASTNode parseAndGetTree(@NotNull PsiBuilder builder) {
    final PsiBuilder.Marker marker = builder.mark();
    parseIntoBuilder(builder);
    marker.done(this);
    return builder.getTreeBuilt();
  }
}
