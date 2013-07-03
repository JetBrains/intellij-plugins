package com.intellij.javascript.karma.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sergey Simonchik
 */
public class GsonUtil {

  private GsonUtil() {}

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
  public static String asString(@NotNull JsonElement element) {
    if (element.isJsonPrimitive()) {
      JsonPrimitive primitive = element.getAsJsonPrimitive();
      if (primitive.isString()) {
        return primitive.getAsString();
      }
    }
    return null;
  }

}
