// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.psi;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.intellij.terraform.hcl.HCLBundle;
import org.intellij.terraform.hil.HILTokenTypes;
import org.jetbrains.annotations.NotNull;

import static com.intellij.psi.TokenType.WHITE_SPACE;
import static org.intellij.terraform.hil.HILElementTypes.R_CURLY;
import static org.intellij.terraform.hil.psi.TerraformTemplateTokenTypes.DATA_LANGUAGE_TOKEN_UNPARSED;

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

  public static boolean templateBlockRecoveryUntil(PsiBuilder builder, int level, IElementType stopTokenType) {
    if (!(builder instanceof HilTemplatingAwarePsiBuilder hilBuilder)) return true;

    // process incomplete branches
    if (HILTokenTypes.getIL_CONTROL_STRUCTURE_START_KEYWORDS().contains(builder.getTokenType())) return false;
    if (HILTokenTypes.getIL_CONTROL_STRUCTURE_END_KEYWORDS().contains(builder.getTokenType()) && hilBuilder.isControlStructureTokenExpected(builder.getTokenType())) return false;

    // process empty tags
    if (lookupClosingBrace(builder)) return true;

    // process invalid tags
    return recoverToToken(builder, stopTokenType);
  }

  private static boolean recoverToToken(PsiBuilder builder, IElementType stopTokenType) {
    PsiBuilder.Marker marker = null;
    var errorAlreadyReported = false;
    IElementType reportedTokenType = null;
    while (!builder.eof() && builder.getTokenType() != stopTokenType) {
      if (!errorAlreadyReported) {
        marker = builder.mark();
        reportedTokenType = builder.getTokenType();
        builder.advanceLexer();
        errorAlreadyReported = true;
      }
      else {
        builder.advanceLexer();
      }
    }
    if (marker != null && reportedTokenType != null) {
      marker.error(HCLBundle.message("parsing.error.recover.to.token", stopTokenType.getDebugName(), reportedTokenType.getDebugName()));
    }

    return true;
  }

  private static boolean lookupClosingBrace(PsiBuilder builder) {
    var index = 0;
    while (builder.rawLookup(index) == WHITE_SPACE) {
      index++;
    }
    var isEmptyTagWithOnlySpacesInside = builder.rawLookup(index) == R_CURLY;
    if (isEmptyTagWithOnlySpacesInside) {
      builder.rawAdvanceLexer(index);
      builder.mark().error(HCLBundle.message("parsing.error.empty.tags.not.allowed"));
    }
    return isEmptyTagWithOnlySpacesInside;
  }

  public static boolean expectForEnd(@NotNull PsiBuilder builder, int level) {
    if (builder instanceof HilTemplatingAwarePsiBuilder hilBuilder) {
      hilBuilder.expectForEnd();
    }
    return true;
  }

  public static boolean expectIfEnd(@NotNull PsiBuilder builder, int level) {
    if (builder instanceof HilTemplatingAwarePsiBuilder hilBuilder) {
      hilBuilder.expectIfEnd();
    }
    return true;
  }

  public static boolean removeForEndExpectation(@NotNull PsiBuilder builder, int level) {
    if (builder instanceof HilTemplatingAwarePsiBuilder hilBuilder) {
      hilBuilder.removeForEndExpectation();
    }
    return true;
  }

  public static boolean removeIfEndExpectation(@NotNull PsiBuilder builder, int level) {
    if (builder instanceof HilTemplatingAwarePsiBuilder hilBuilder) {
      hilBuilder.removeIfEndExpectation();
    }
    return true;
  }
}
