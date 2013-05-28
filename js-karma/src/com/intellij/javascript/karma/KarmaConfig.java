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

        //String pattern = getAsString()
        JsonElement patternElement = object.get("pattern");
        //Entry entry = new Entry(o);
      }
      if (fileElement.isJsonPrimitive()) {
        JsonPrimitive primitive = fileElement.getAsJsonPrimitive();
        if (primitive.isString()) {
          files.add(primitive.getAsString());
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
  private static Boolean getAsBoolean(@NotNull JsonElement element) {
    if (element.isJsonPrimitive()) {
      JsonPrimitive primitive = element.getAsJsonPrimitive();
      if (primitive.isBoolean()) {
        return primitive.getAsBoolean();
      }
    }
    return null;
  }

  private static class Entry {
    private final String myPattern;
    private final boolean myServed;
    private final boolean myIncluded;
    private final boolean myWatched;

    private Entry(@NotNull String pattern, boolean served, boolean included, boolean watched) {
      myPattern = pattern;
      myServed = served;
      myIncluded = included;
      myWatched = watched;
    }

    @NotNull
    private String getPattern() {
      return myPattern;
    }

    private boolean isServed() {
      return myServed;
    }

    private boolean isIncluded() {
      return myIncluded;
    }

    private boolean isWatched() {
      return myWatched;
    }
  }

}
