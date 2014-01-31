package com.jetbrains.lang.dart;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.openapi.util.Key;

import static com.jetbrains.lang.dart.DartTokenTypes.ID;
import static com.jetbrains.lang.dart.DartTokenTypes.IDENTIFIER;

public class DartGeneratedParserUtilBase extends GeneratedParserUtilBase {
  private static Key<Boolean> WITHOUT_CASCADE = Key.create("dart.without.cascade");

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

  public static boolean nonStrictID(PsiBuilder builder_, int level_) {
    final PsiBuilder.Marker marker_ = builder_.mark();
    final boolean result_ = consumeToken(builder_, IDENTIFIER);
    if (result_) {
      marker_.done(ID);
      return true;
    }
    else if (DartTokenTypesSets.BUILT_IN_IDENTIFIERS.contains(builder_.getTokenType())) {
      builder_.advanceLexer();
      marker_.done(ID);
      return true;
    }
    else {
      marker_.rollbackTo();
    }
    return false;
  }
}
