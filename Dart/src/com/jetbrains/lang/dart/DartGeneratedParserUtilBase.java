// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderUtil;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.openapi.util.Key;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

import static com.jetbrains.lang.dart.DartTokenTypes.*;
import static com.jetbrains.lang.dart.DartTokenTypesSets.LAZY_PARSEABLE_BLOCK;

public class DartGeneratedParserUtilBase extends GeneratedParserUtilBase {
  private static final Key<Boolean> WITHOUT_CASCADE = Key.create("dart.without.cascade");
  static final Key<Boolean> INSIDE_SYNC_OR_ASYNC_FUNCTION = Key.create("dart.inside.sync.or.async.function");

  private static final TokenSet IDENTIFIERS_FORBIDDEN_INSIDE_ASYNC_FUNCTIONS = TokenSet.create(AWAIT, YIELD);

  public static boolean cascadeStopper(PsiBuilder builder_, int level_) {
    Boolean userData = builder_.getUserData(WITHOUT_CASCADE);
    boolean fail = userData != null && userData;
    return !fail;
  }

  public static boolean varInitWrapper(PsiBuilder builder_, int level_) {
    builder_.putUserData(WITHOUT_CASCADE, true);
    DartParser.varInit(builder_, level_);
    builder_.putUserData(WITHOUT_CASCADE, false);
    return true;
  }

  public static boolean parenthesizedExpressionWrapper(PsiBuilder builder_, int level_) {
    final Boolean cascadeData = builder_.getUserData(WITHOUT_CASCADE);
    builder_.putUserData(WITHOUT_CASCADE, null);
    final boolean result = DartParser.parenthesizedExpression(builder_, level_);
    builder_.putUserData(WITHOUT_CASCADE, cascadeData);
    return result;
  }

  public static boolean argumentsWrapper(PsiBuilder builder_, int level_) {
    final Boolean cascadeData = builder_.getUserData(WITHOUT_CASCADE);
    builder_.putUserData(WITHOUT_CASCADE, null);
    final boolean result = DartParser.arguments(builder_, level_);
    builder_.putUserData(WITHOUT_CASCADE, cascadeData);
    return result;
  }

  public static boolean mapLiteralExpressionWrapper(PsiBuilder builder_, int level_) {
    final Boolean cascadeData = builder_.getUserData(WITHOUT_CASCADE);
    builder_.putUserData(WITHOUT_CASCADE, null);
    final boolean result = DartParser.mapLiteralExpression(builder_, level_);
    builder_.putUserData(WITHOUT_CASCADE, cascadeData);
    return result;
  }

  public static boolean setLiteralExpressionWrapper(PsiBuilder builder_, int level_) {
    final Boolean cascadeData = builder_.getUserData(WITHOUT_CASCADE);
    builder_.putUserData(WITHOUT_CASCADE, null);
    final boolean result = DartParser.setLiteralExpression(builder_, level_);
    builder_.putUserData(WITHOUT_CASCADE, cascadeData);
    return result;
  }

  public static boolean listLiteralExpressionWrapper(PsiBuilder builder_, int level_) {
    final Boolean cascadeData = builder_.getUserData(WITHOUT_CASCADE);
    builder_.putUserData(WITHOUT_CASCADE, null);
    final boolean result = DartParser.listLiteralExpression(builder_, level_);
    builder_.putUserData(WITHOUT_CASCADE, cascadeData);
    return result;
  }

  public static boolean methodNameWrapper(PsiBuilder builder_, int level_) {
    if (nextTokenIs(builder_, OPERATOR)) {
      final PsiBuilder.Marker marker0 = builder_.mark();
      consumeToken(builder_, OPERATOR);
      final PsiBuilder.Marker marker1 = builder_.mark();
      final PsiBuilder.Marker marker2 = builder_.mark();
      final PsiBuilder.Marker marker3 = builder_.mark();
      if (DartParser.userDefinableOperator(builder_, level_)) {
        marker3.collapse(IDENTIFIER);
        marker2.done(ID);
        marker1.done(COMPONENT_NAME);
        marker0.drop();
        return true;
      }
      marker3.rollbackTo();
      marker2.rollbackTo();
      marker1.rollbackTo();
      marker0.rollbackTo();
    }
    return DartParser.componentName(builder_, level_);
  }

  public static boolean nonStrictID(PsiBuilder builder_, int level_) {
    final PsiBuilder.Marker marker_ = builder_.mark();
    final boolean result_ = consumeToken(builder_, IDENTIFIER);
    if (result_) {
      marker_.done(ID);
      return true;
    }
    else if (Boolean.TRUE == builder_.getUserData(INSIDE_SYNC_OR_ASYNC_FUNCTION) &&
             IDENTIFIERS_FORBIDDEN_INSIDE_ASYNC_FUNCTIONS.contains(builder_.getTokenType())) {
      marker_.rollbackTo();
      return false;
    }
    else if (DartTokenTypesSets.BUILT_IN_IDENTIFIERS.contains(builder_.getTokenType())) {
      builder_.advanceLexer();
      marker_.done(ID);
      return true;
    }
    marker_.rollbackTo();
    return false;
  }

  public static boolean functionId(PsiBuilder builder_, int level_) {
    if (!"Function".equals(builder_.getTokenText())) return false;

    final PsiBuilder.Marker marker_ = builder_.mark();
    final boolean result_ = consumeToken(builder_, IDENTIFIER);
    if (result_) {
      marker_.done(ID);
      return true;
    }

    marker_.rollbackTo();
    return false;
  }

  public static boolean lazyParseableBlockImpl(PsiBuilder builder, int level) {
    return PsiBuilderUtil.parseBlockLazy(builder, LBRACE, RBRACE, LAZY_PARSEABLE_BLOCK) != null;
  }

  public static boolean arrowBodyWrapper(PsiBuilder builder_, int level_) {
    final Boolean wasSyncOrAsync = builder_.getUserData(INSIDE_SYNC_OR_ASYNC_FUNCTION);
    final boolean syncOrAsync = nextTokenIs(builder_, "", ASYNC, SYNC);
    builder_.putUserData(INSIDE_SYNC_OR_ASYNC_FUNCTION, syncOrAsync ? Boolean.TRUE : null);
    final boolean result = DartParser.arrowBody(builder_, level_);
    builder_.putUserData(INSIDE_SYNC_OR_ASYNC_FUNCTION, wasSyncOrAsync);
    return result;
  }

  public static boolean blockBodyWrapper(PsiBuilder builder_, int level_) {
    final Boolean cascadeData = builder_.getUserData(WITHOUT_CASCADE);
    builder_.putUserData(WITHOUT_CASCADE, null);

    final Boolean wasSyncOrAsync = builder_.getUserData(INSIDE_SYNC_OR_ASYNC_FUNCTION);
    final boolean syncOrAsync = nextTokenIs(builder_, "", ASYNC, SYNC);
    builder_.putUserData(INSIDE_SYNC_OR_ASYNC_FUNCTION, syncOrAsync ? Boolean.TRUE : null);

    final boolean result = DartParser.blockBody(builder_, level_);

    builder_.putUserData(INSIDE_SYNC_OR_ASYNC_FUNCTION, wasSyncOrAsync);

    builder_.putUserData(WITHOUT_CASCADE, cascadeData);

    return result;
  }

  public static boolean gtGt(PsiBuilder builder_, int level_) {
    final PsiBuilder.Marker marker_ = builder_.mark();
    if (!consumeToken(builder_, GT)) {
      marker_.rollbackTo();
      return false;
    }
    if (!consumeToken(builder_, GT)) {
      marker_.rollbackTo();
      return false;
    }
    marker_.collapse(GT_GT);
    return true;
  }

  public static boolean gtEq(PsiBuilder builder_, int level_) {
    final PsiBuilder.Marker marker_ = builder_.mark();
    if (!consumeToken(builder_, GT)) {
      marker_.rollbackTo();
      return false;
    }
    if (!consumeToken(builder_, EQ)) {
      marker_.rollbackTo();
      return false;
    }
    marker_.collapse(GT_EQ);
    return true;
  }

  public static boolean gtGtEq(PsiBuilder builder_, int level_) {
    final PsiBuilder.Marker marker_ = builder_.mark();
    if (!consumeToken(builder_, GT)) {
      marker_.rollbackTo();
      return false;
    }
    if (!consumeToken(builder_, GT)) {
      marker_.rollbackTo();
      return false;
    }
    if (!consumeToken(builder_, EQ)) {
      marker_.rollbackTo();
      return false;
    }
    marker_.collapse(GT_GT_EQ);
    return true;
  }

  public static boolean failIfItLooksLikeConstantObjectExpression(PsiBuilder builder_, int level_) {
    // need to fail varAccessDeclaration parsing if this looks like constant object expression (it will be parsed later by DartNewExpression)
    final IElementType type = builder_.getTokenType();
    return type != DOT && type != LPAREN && type != LT;
  }

  public static boolean isInsideSyncOrAsyncFunction(PsiBuilder builder_, int level_) {
    return builder_.getUserData(INSIDE_SYNC_OR_ASYNC_FUNCTION) == Boolean.TRUE;
  }
}