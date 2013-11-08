package com.jetbrains.lang.dart;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.openapi.util.Key;
import gnu.trove.THashSet;

import java.util.Arrays;
import java.util.Set;

import static com.jetbrains.lang.dart.DartTokenTypes.ID;
import static com.jetbrains.lang.dart.DartTokenTypes.IDENTIFIER;

public class DartGeneratedParserUtilBase extends GeneratedParserUtilBase {
  private static Key<Boolean> WITHOUT_CASCADE = Key.create("dart.without.cascade");

  // todo: operator is pseudoKeyword
  private static final Set<String> pseudoKeywords = new THashSet<String>(Arrays.asList(
    "abstract", "assert", "class", "extends", "factory", "implements", "import", "interface",
    "is", "as", "on", "library", "native", "source", "static", "typedef", "operator",
    "set", "get", "of", "part", "show", "hide", "export", "with"
  ));

  private static final Set<String> operators = new THashSet<String>(Arrays.asList(
    "%", "&", "*", "+", "-", "/", "<", "<<", "<=", "==", ">", ">=", "[", "^", "|", "~", "~/"
  ));

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
    else if ("get".equals(builder_.getTokenText()) || "set".equals(builder_.getTokenText())) {
      builder_.advanceLexer();
      final boolean nextIsID = builder_.getTokenType() == IDENTIFIER || pseudoKeywords.contains(builder_.getTokenText());
      if (!nextIsID) {
        marker_.done(ID);
        return true;
      }
    }
    else if ("operator".equals(builder_.getTokenText())) {
      builder_.advanceLexer();
      if (!operators.contains(builder_.getTokenText())) {
        marker_.done(ID);
        return true;
      }
    }
    else if (pseudoKeywords.contains(builder_.getTokenText())) {
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
