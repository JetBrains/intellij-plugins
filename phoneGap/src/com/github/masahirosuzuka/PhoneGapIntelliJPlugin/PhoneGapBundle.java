package com.github.masahirosuzuka.PhoneGapIntelliJPlugin;


import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.function.Supplier;

public class PhoneGapBundle extends DynamicBundle {

  public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
    return ourInstance.getMessage(key, params);
  }

  @NotNull
  public static Supplier<String> lazyMessage(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
    return ourInstance.getLazyMessage(key, params);
  }

  @NonNls public static final String BUNDLE = "messages.PhoneGapBundle";
  private static final PhoneGapBundle ourInstance = new PhoneGapBundle();

  private PhoneGapBundle() {
    super(BUNDLE);
  }
}
