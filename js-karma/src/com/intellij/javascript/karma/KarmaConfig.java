package com.intellij.javascript.karma;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sergey Simonchik
 */
public class KarmaConfig {

  private static final String BASE_PATH_NAME = "basePath";

  private final String myBasePath;

  public KarmaConfig(@Nullable String basePath) {
    myBasePath = basePath;
  }

  @Nullable
  public String getBasePath() {
    return myBasePath;
  }

  public static KarmaConfig parseFromJson(@NotNull String jsonText) {
    JsonParser jsonParser = new JsonParser();
    JsonElement jsonElement = jsonParser.parse(jsonText);
    if (jsonElement.isJsonObject()) {
      JsonObject rootObject = jsonElement.getAsJsonObject();
      JsonElement basePathElement = rootObject.get(BASE_PATH_NAME);
      if (basePathElement != null && basePathElement.isJsonPrimitive()) {
        JsonPrimitive basePathPrimitive = basePathElement.getAsJsonPrimitive();
        if (basePathPrimitive.isString()) {
          return new KarmaConfig(basePathPrimitive.getAsString());
        }
      }
    }
    return null;
  }
}
