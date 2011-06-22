package com.google.jstestdriver.idea.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ObjectUtils {

  private ObjectUtils() {}

  @NotNull
  public static <T, K extends T> T notNull(@Nullable T value, @NotNull K defaultValue) {
    return value == null ? defaultValue : value;
  }

  @Nullable
  public static <T, K extends T> T defaultIfNull(@Nullable T value, @Nullable K defaultValue) {
    return value == null ? defaultValue : value;
  }

}
