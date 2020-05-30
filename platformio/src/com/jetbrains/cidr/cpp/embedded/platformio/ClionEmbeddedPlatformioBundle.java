package com.jetbrains.cidr.cpp.embedded.platformio;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.function.Supplier;

public class ClionEmbeddedPlatformioBundle extends DynamicBundle {
  @NonNls private static final String BUNDLE = "messages.ClionEmbeddedPlatformioBundle";
  private static final ClionEmbeddedPlatformioBundle INSTANCE = new ClionEmbeddedPlatformioBundle();

  private ClionEmbeddedPlatformioBundle() {
    super(BUNDLE);
  }

  @NotNull
  public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
    return INSTANCE.getMessage(key, params);
  }

  @NotNull
  public static Supplier<String> messagePointer(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
    return INSTANCE.getLazyMessage(key, params);
  }
}
