package com.google.jstestdriver.idea.util;

public class EnumUtils {

  private EnumUtils() {}

  public static <E extends Enum<E>> E findEnum(Class<E> enumClass, String name) {
    return findEnum(enumClass, name, true);
  }

  public static <E extends Enum<E>> E findEnum(Class<E> enumClass, String name, boolean caseSensitive) {
    E[] enumConstants = enumClass.getEnumConstants();
    for (E e : enumConstants) {
      if (caseSensitive ? e.name().equals(name) : e.name().equalsIgnoreCase(name)) {
        return e;
      }
    }
    return null;
  }

}
