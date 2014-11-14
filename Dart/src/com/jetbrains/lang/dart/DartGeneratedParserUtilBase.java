package com.jetbrains.lang.dart;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.openapi.util.Key;
import com.intellij.psi.tree.TokenSet;

import static com.jetbrains.lang.dart.DartTokenTypes.*;

public class DartGeneratedParserUtilBase extends GeneratedParserUtilBase {
  private static final Key<Boolean> WITHOUT_CASCADE = Key.create("dart.without.cascade");
  private static final Key<Boolean> INSIDE_SYNC_OR_ASYNC_FUNCTION = Key.create("dart.inside.sync.or.async.function");

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

  public static boolean mapLiteralExpressionWrapper(PsiBuilder builder_, int level_) {
    final Boolean cascadeData = builder_.getUserData(WITHOUT_CASCADE);
    builder_.putUserData(WITHOUT_CASCADE, null);
    final boolean result = DartParser.mapLiteralExpression(builder_, level_);
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

  public static boolean arrowBodyWrapper(PsiBuilder builder_, int level_) {
    final Boolean wasSyncOrAsync = builder_.getUserData(INSIDE_SYNC_OR_ASYNC_FUNCTION);
    final boolean syncOrAsync = nextTokenIs(builder_, "", ASYNC, SYNC);
    builder_.putUserData(INSIDE_SYNC_OR_ASYNC_FUNCTION, syncOrAsync ? Boolean.TRUE : null);
    final boolean result = DartParser.arrowBody(builder_, level_);
    builder_.putUserData(INSIDE_SYNC_OR_ASYNC_FUNCTION, wasSyncOrAsync);
    return result;
  }

  public static boolean blockBodyWrapper(PsiBuilder builder_, int level_) {
    final Boolean wasSyncOrAsync = builder_.getUserData(INSIDE_SYNC_OR_ASYNC_FUNCTION);
    final boolean syncOrAsync = nextTokenIs(builder_, "", ASYNC, SYNC);
    builder_.putUserData(INSIDE_SYNC_OR_ASYNC_FUNCTION, syncOrAsync ? Boolean.TRUE : null);
    final boolean result = DartParser.blockBody(builder_, level_);
    builder_.putUserData(INSIDE_SYNC_OR_ASYNC_FUNCTION, wasSyncOrAsync);
    return result;
  }
}
