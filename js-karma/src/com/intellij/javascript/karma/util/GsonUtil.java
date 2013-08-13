package com.intellij.javascript.karma.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sergey Simonchik
 */
public class GsonUtil {

  private GsonUtil() {}

  public static boolean getBooleanProperty(@NotNull JsonObject jsonObject,
                                           @NotNull String propertyName,
                                           boolean defaultValue) {
    return ObjectUtils.notNull(getBooleanProperty(jsonObject, propertyName), defaultValue);
  }

  @Nullable
  public static Boolean getBooleanProperty(@NotNull JsonObject jsonObject, @NotNull String propertyName) {
    JsonElement jsonElement = jsonObject.get(propertyName);
    if (jsonElement != null) {
      if (jsonElement.isJsonPrimitive()) {
        JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();
        if (primitive.isBoolean()) {
          return primitive.getAsBoolean();
        }
      }
    }
    return null;
  }

  @Nullable
  public static String getStringProperty(@NotNull JsonObject jsonObject, @NotNull String propertyName) {
    JsonElement jsonElement = jsonObject.get(propertyName);
    if (jsonElement != null) {
      if (jsonElement.isJsonPrimitive()) {
        JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();
        if (primitive.isString()) {
          return primitive.getAsString();
        }
      }
    }
    return null;
  }

  @Nullable
  public static String getAsString(@Nullable JsonElement element) {
    if (element != null && element.isJsonPrimitive()) {
      JsonPrimitive primitive = element.getAsJsonPrimitive();
      if (primitive.isString()) {
        return primitive.getAsString();
      }
    }
    return null;
  }

}
