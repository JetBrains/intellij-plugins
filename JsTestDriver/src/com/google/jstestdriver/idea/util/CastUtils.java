package com.google.jstestdriver.idea.util;

import org.jetbrains.annotations.Nullable;

public class CastUtils {

  private CastUtils() {}

  @Nullable
  public static <T> T tryCast(Object o, Class<T> clazz) {
    if (clazz.isInstance(o)) {
      return clazz.cast(o);
    }
    return null;
  }

}
