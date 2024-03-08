// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.psi;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
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

  public static boolean templateBlockRecoveryUntil(PsiBuilder b, int l, IElementType stopTokenType) {
    if (!(b instanceof HilTemplatingAwarePsiBuilder hilBuilder)) return false;

    // process incomplete branches
    if (HILTokenTypes.getIL_CONTROL_STRUCTURE_START_KEYWORDS().contains(b.getTokenType())) return false;
    if (HILTokenTypes.getIL_CONTROL_STRUCTURE_END_KEYWORDS().contains(b.getTokenType()) && hilBuilder.isControlStructureTokenExpected(b.getTokenType())) return false;

    // process empty tags
    if (lookupClosingBrace(b, l)) return true;

    // process invalid tags
    return recoverToToken(b, l, true, stopTokenType);
  }

  private static boolean recoverToToken(PsiBuilder b, int l, boolean reportError, IElementType stopTokenType) {
    PsiBuilder.Marker marker = null;
    while (!b.eof() && b.getTokenType() != stopTokenType) {
      if (reportError) {
        marker = b.mark();
        b.advanceLexer();
        reportError = false;
      }
      else {
        b.advanceLexer();
      }
    }
    if (marker != null) {
      marker.error("RECOVER TO TOKEN");
    }

    return true;
  }

  private static boolean lookupClosingBrace(PsiBuilder b, int l) {
    var index = 0;
    while (b.rawLookup(index) == WHITE_SPACE) {
      index++;
    }
    var isEmptyTagWithOnlySpacesInside = b.rawLookup(index) == R_CURLY;
    if (isEmptyTagWithOnlySpacesInside) {
      b.rawAdvanceLexer(index);
      b.mark().error("Empty tags not allowed");
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
