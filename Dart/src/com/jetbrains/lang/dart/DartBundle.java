package com.jetbrains.lang.dart;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public class DartBundle extends DynamicBundle {
  @NonNls private static final String BUNDLE = "com.jetbrains.lang.dart.DartBundle";
  private static final DartBundle INSTANCE = new DartBundle();

  private DartBundle() { super(BUNDLE); }

  @NotNull
  public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, @NotNull Object... params) {
    return INSTANCE.getMessage(key, params);
  }
}
