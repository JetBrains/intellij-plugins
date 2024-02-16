// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.psi;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

import static org.intellij.terraform.template.lexer.TerraformTemplateTokenTypes.DATA_LANGUAGE_TOKEN_UNPARSED;

public class HILParserUtil extends GeneratedParserUtilBase {
  @SuppressWarnings("MethodOverridesStaticMethodOfSuperclass")
  public static PsiBuilder adapt_builder_(IElementType root, PsiBuilder builder, PsiParser parser, TokenSet[] extendsSets) {
    ErrorState state = new ErrorState();
    ErrorState.initState(state, builder, root, extendsSets);
    return new HilTemplatingAwarePsiBuilder(builder, state, parser);
  }

  public static boolean isTemplatingSupported(PsiBuilder builder, int level) {
    return builder instanceof HilTemplatingAwarePsiBuilder templatingAwareBuilder && templatingAwareBuilder.isTemplatingSupported();
  }

  public static boolean parseDataLanguageToken(PsiBuilder builder, int level) {
    if (!isDataLanguageToken(builder, level)) {
      return false;
    }

    PsiBuilder.Marker marker = builder.mark();
    while (isDataLanguageToken(builder, level)) {
      builder.advanceLexer();
    }

    marker.done(DATA_LANGUAGE_TOKEN_UNPARSED);
    return true;
  }

  public static boolean isDataLanguageToken(PsiBuilder builder, int level) {
    return DATA_LANGUAGE_TOKEN_UNPARSED.equals(builder.getTokenType());
  }
}
