package com.intellij.prettierjs;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public class PrettierBundle extends DynamicBundle {
  @NonNls public static final String BUNDLE = "PrettierBundle";
  private static final PrettierBundle INSTANCE = new PrettierBundle();

  private PrettierBundle() { super(BUNDLE); }

  @NotNull
  public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, @NotNull Object... params) {
    return INSTANCE.getMessage(key, params);
  }
}
