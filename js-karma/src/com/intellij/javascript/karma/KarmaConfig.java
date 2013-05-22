package com.intellij.javascript.karma;

import com.google.common.collect.Sets;
import com.google.gson.*;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.Set;

/**
 * @author Sergey Simonchik
 */
public class KarmaConfig {

  private static final String BASE_PATH_NAME = "basePath";
  private static final String FILES_NAME = "files";

  private final String myBasePath;
  private Set<VirtualFile> myFiles;

  public KarmaConfig(@Nullable String basePath, @NotNull Set<VirtualFile> files) {
    myBasePath = basePath;
    myFiles = files;
  }

  @Nullable
  public String getBasePath() {
    return myBasePath;
  }

  @NotNull
  public Set<VirtualFile> getFiles() {
    return myFiles;
  }

  public static KarmaConfig parseFromJson(@NotNull String jsonText) {
    JsonParser jsonParser = new JsonParser();
    JsonElement jsonElement = jsonParser.parse(jsonText);
    if (jsonElement.isJsonObject()) {
      JsonObject rootObject = jsonElement.getAsJsonObject();
      String basePath = buildBasePath(rootObject.get(BASE_PATH_NAME));
      Set<VirtualFile> files = buildFileSet(rootObject.get(FILES_NAME));
      return new KarmaConfig(basePath, files);
    }
    return null;
  }

  @Nullable
  private static String buildBasePath(@Nullable JsonElement basePathElement) {
    if (basePathElement == null || !basePathElement.isJsonPrimitive()) {
      return null;
    }
    JsonPrimitive basePathPrimitive = basePathElement.getAsJsonPrimitive();
    if (basePathPrimitive.isString()) {
      return basePathPrimitive.getAsString();
    }
    return null;
  }

  @NotNull
  private static Set<VirtualFile> buildFileSet(@Nullable JsonElement filesElement) {
    if (filesElement == null || !filesElement.isJsonArray()) {
      return Collections.emptySet();
    }
    JsonArray filesArray = filesElement.getAsJsonArray();
    Set<VirtualFile> files = Sets.newHashSetWithExpectedSize(filesArray.size());
    for (JsonElement element : filesArray) {
      if (element.isJsonPrimitive()) {
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isString()) {
          File file = new File(primitive.getAsString());
          files.add(VfsUtil.findFileByIoFile(file, false));
        }
      }
    }
    return files;
  }

}
