package com.google.jstestdriver.idea.util;

public class CastUtils {

  private CastUtils() {}

  public static <T> T tryCast(Object o, Class<T> clazz) {
    if (clazz.isInstance(o)) {
      return clazz.cast(o);
    }
    return null;
  }

}
