package com.jetbrains.lang.dart.util;

import com.intellij.codeInsight.completion.CompletionInitializationContext;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;

/**
 * @author: Fedor.Korotkov
 */
public class DartCodeGenerateUtil {
  public static final String CLASS_PREFIX = "class Main{";
  public static final String CLASS_SUFFIX = "}";
  public static final String FUNCTION_PREFIX = "main(){";
  public static final String FUNCTION_SUFFIX = "}";

  public static Pair<String, Integer> wrapStatement(String statement) {
    statement = trimDummy(statement);
    final String function = FUNCTION_PREFIX + statement + FUNCTION_SUFFIX;
    final Pair<String, Integer> pair = wrapFunction(function);
    return new Pair<>(pair.getFirst(), pair.getSecond() + FUNCTION_SUFFIX.length());
  }

  public static Pair<String, Integer> wrapFunction(String function) {
    function = trimDummy(function);
    return new Pair<>(CLASS_PREFIX + function + CLASS_SUFFIX, CLASS_SUFFIX.length());
  }

  private static String trimDummy(String text) {
    text = StringUtil.trimEnd(text, CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED);
    return text;
  }
}
