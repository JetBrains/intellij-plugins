package com.intellij.javascript.karma.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @deprecated use {@link com.intellij.webcore.util.JsonUtil} instead
 */
public class GsonUtil {

  private GsonUtil() {}

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
}
