package com.intellij.javascript.karma;

import com.google.common.collect.Sets;
import com.google.gson.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

/**
 * @author Sergey Simonchik
 */
public class KarmaConfig {

  private static final String BASE_PATH_NAME = "basePath";
  private static final String FILES_NAME = "files";

  private final String myBasePath;
  private Set<String> myFiles;

  public KarmaConfig(@Nullable String basePath, @NotNull Set<String> files) {
    myBasePath = basePath;
    myFiles = files;
  }

  @Nullable
  public String getBasePath() {
    return myBasePath;
  }

  @NotNull
  public Set<String> getFiles() {
    return myFiles;
  }

  public static KarmaConfig parseFromJson(@NotNull String jsonText) {
    JsonParser jsonParser = new JsonParser();
    JsonElement jsonElement = jsonParser.parse(jsonText);
    if (jsonElement.isJsonObject()) {
      JsonObject rootObject = jsonElement.getAsJsonObject();
      String basePath = getAsString(rootObject.get(BASE_PATH_NAME));
      Set<String> files = buildFileSet(rootObject.get(FILES_NAME));
      return new KarmaConfig(basePath, files);
    }
    return null;
  }

  @NotNull
  private static Set<String> buildFileSet(@Nullable JsonElement filesElement) {
    if (filesElement == null || !filesElement.isJsonArray()) {
      return Collections.emptySet();
    }
    JsonArray filesArray = filesElement.getAsJsonArray();
    Set<String> files = Sets.newHashSetWithExpectedSize(filesArray.size());
    for (JsonElement fileElement : filesArray) {
      if (fileElement.isJsonObject()) {
        JsonObject object = fileElement.getAsJsonObject();
        String pattern = getAsString(object.get("pattern"));
        Boolean included = getAsBoolean(object.get("included"));
        if (pattern != null && Boolean.TRUE.equals(included)) {
          files.add(pattern);
        }
      }
    }
    return files;
  }

  @Nullable
  private static String getAsString(@Nullable JsonElement element) {
    if (element != null && element.isJsonPrimitive()) {
      JsonPrimitive primitive = element.getAsJsonPrimitive();
      if (primitive.isString()) {
        return primitive.getAsString();
      }
    }
    return null;
  }

  @Nullable
  private static Boolean getAsBoolean(@Nullable JsonElement element) {
    if (element != null && element.isJsonPrimitive()) {
      JsonPrimitive primitive = element.getAsJsonPrimitive();
      if (primitive.isBoolean()) {
        return primitive.getAsBoolean();
      }
    }
    return null;
  }

}
