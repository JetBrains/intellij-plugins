package com.intellij.lang.javascript.linter.tslint;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public class TsLintBundle extends DynamicBundle {
  @NonNls private static final String BUNDLE = "messages.TsLintBundle";
  private static final TsLintBundle INSTANCE = new TsLintBundle();

  private TsLintBundle() { super(BUNDLE); }

  @NotNull
  public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, @NotNull Object... params) {
    return INSTANCE.getMessage(key, params);
  }
}
