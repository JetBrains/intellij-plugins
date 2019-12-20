package com.dmarcotte.handlebars;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public class HbBundle extends DynamicBundle {
  @NonNls private static final String BUNDLE = "messages.HbBundle";
  private static final HbBundle INSTANCE = new HbBundle();

  private HbBundle() { super(BUNDLE); }

  @NotNull
  public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, @NotNull Object... params) {
    return INSTANCE.getMessage(key, params);
  }
}
