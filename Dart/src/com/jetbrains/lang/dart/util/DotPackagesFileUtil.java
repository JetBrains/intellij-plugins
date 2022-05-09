// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.OSAgnosticPathUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.io.URLUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DotPackagesFileUtil {

  public static final String DOT_PACKAGES = ".packages";

  public static final String DART_TOOL_DIR = ".dart_tool";
  public static final String PACKAGE_CONFIG_JSON = "package_config.json";

  private static final Key<Pair<Long, Map<String, String>>> MOD_STAMP_TO_PACKAGES_MAP = Key.create("MOD_STAMP_TO_PACKAGES_MAP");

  /**
   * Starting with some {@link VirtualFile}, search the parent directories for some instance of .dart_tool/package_config.json.
   */
  public static @Nullable VirtualFile findPackageConfigJsonFile(@Nullable VirtualFile vFile) {
    if (vFile == null) {
      return null;
    }
    VirtualFile dir = vFile.isDirectory() ? vFile : vFile.getParent();
    while (dir != null) {
      VirtualFile dartToolDir = dir.findChild(DART_TOOL_DIR);
      if (dartToolDir != null && dartToolDir.isDirectory()) {
        return dartToolDir.findChild(PACKAGE_CONFIG_JSON);
      }
      dir = dir.getParent();
    }
    return null;
  }

  /**
   * Given a file Dart pub root (either the yaml pubspec {@link VirtualFile} or the parent of a yaml pubspec), return the
   * .dart_tool/package_config.json {@link VirtualFile}, if it exists.
   */
  public static @Nullable VirtualFile getPackageConfigJsonFile(@NotNull VirtualFile pubspecYamlFile) {
    VirtualFile dir = pubspecYamlFile.getParent();
    VirtualFile dartToolDir = dir.findChild(DART_TOOL_DIR);
    if (dartToolDir != null && dartToolDir.isDirectory()) {
      return dartToolDir.findChild(PACKAGE_CONFIG_JSON);
    }
    return null;
  }

  public static @Nullable VirtualFile findDotPackagesFile(@Nullable VirtualFile dir) {
    while (dir != null) {
      final VirtualFile file = dir.findChild(DOT_PACKAGES);
      if (file != null && !file.isDirectory()) {
        return file;
      }
      dir = dir.getParent();
    }
    return null;
  }

  public static @Nullable Map<String, String> getPackagesMapFromPackageConfigJsonFile(final @NotNull VirtualFile packageConfigJsonFile) {
    Pair<Long, Map<String, String>> data = packageConfigJsonFile.getUserData(MOD_STAMP_TO_PACKAGES_MAP);

    final Long currentTimestamp = packageConfigJsonFile.getModificationCount();
    final Long cachedTimestamp = Pair.getFirst(data);

    if (cachedTimestamp == null || !cachedTimestamp.equals(currentTimestamp)) {
      data = null;
      packageConfigJsonFile.putUserData(MOD_STAMP_TO_PACKAGES_MAP, null);
      final Map<String, String> packagesMap = loadPackagesMapFromJson(packageConfigJsonFile);

      if (packagesMap != null) {
        data = Pair.create(currentTimestamp, packagesMap);
        packageConfigJsonFile.putUserData(MOD_STAMP_TO_PACKAGES_MAP, data);
      }
    }

    return Pair.getSecond(data);
  }

  /**
   * Example JSON parsed:
   * ```
   * {
   * "configVersion": 2,
   * "packages": [
   * {
   * "name": "pedantic",
   * "rootUri": "file:///Users/jwren/.pub-cache/hosted/pub.dartlang.org/pedantic-1.11.0",
   * "packageUri": "lib/",
   * "languageVersion": "2.12"
   * },
   * {
   * "name": "console_dart",
   * "rootUri": "../",
   * "packageUri": "lib/",
   * "languageVersion": "2.12"
   * }
   * ],
   * "generated": "2022-03-04T22:10:17.132325Z",
   * "generator": "pub",
   * "generatorVersion": "2.17.0-edge.f9147d933ef019c7e304c19ac039f57226ce1e37"
   * }
   * ```
   */
  private static @Nullable Map<String, String> loadPackagesMapFromJson(@NotNull VirtualFile packageConfigJsonFile) {
    String fileContentsStr = FileUtil.loadFileOrNull(packageConfigJsonFile.getPath());
    if (fileContentsStr == null) {
      return null;
    }

    final JsonElement jsonElement;
    try {
      jsonElement = JsonParser.parseString(fileContentsStr);
    }
    catch (Exception e) {
      Logger.getInstance(DotPackagesFileUtil.class).info(e);
      return null;
    }

    if (jsonElement instanceof JsonObject &&
        ((JsonObject)jsonElement).get("packages") != null &&
        ((JsonObject)jsonElement).get("packages").isJsonArray()) {
      final Map<String, String> result = new HashMap<>();
      final JsonArray jsonArray = ((JsonObject)jsonElement).get("packages").getAsJsonArray();
      for (JsonElement element : jsonArray) {
        JsonObject jsonObjectPackage = element.getAsJsonObject();
        if (jsonObjectPackage.get("name") != null && jsonObjectPackage.get("name").isJsonPrimitive() &&
            jsonObjectPackage.get("rootUri") != null && jsonObjectPackage.get("rootUri").isJsonPrimitive() &&
            jsonObjectPackage.get("packageUri") != null && jsonObjectPackage.get("packageUri").isJsonPrimitive()
        ) {
          final String packageName = jsonObjectPackage.get("name").getAsString();
          final String rootUriValue = jsonObjectPackage.get("rootUri").getAsString();
          final String packageUriValue = jsonObjectPackage.get("packageUri").getAsString();
          // need to protect '+' chars because URLDecoder.decode replaces '+' with space
          final String encodedUriWithoutPluses = StringUtil.replace(rootUriValue + "/" + packageUriValue, "+", "%2B");
          final String uri = URLUtil.decode(encodedUriWithoutPluses);
          final String packageUri = getAbsolutePackageRootPath(packageConfigJsonFile.getParent(), uri);
          if (!packageName.isEmpty() && packageUri != null) {
            result.put(packageName, packageUri);
          }
        }
      }
      return result;
    }
    return null;
  }

  public static @Nullable Map<String, String> getPackagesMap(final @NotNull VirtualFile dotPackagesFile) {
    Pair<Long, Map<String, String>> data = dotPackagesFile.getUserData(MOD_STAMP_TO_PACKAGES_MAP);

    final Long currentTimestamp = dotPackagesFile.getModificationCount();
    final Long cachedTimestamp = Pair.getFirst(data);

    if (cachedTimestamp == null || !cachedTimestamp.equals(currentTimestamp)) {
      data = null;
      dotPackagesFile.putUserData(MOD_STAMP_TO_PACKAGES_MAP, null);
      final Map<String, String> packagesMap = loadPackagesMap(dotPackagesFile);

      if (packagesMap != null) {
        data = Pair.create(currentTimestamp, packagesMap);
        dotPackagesFile.putUserData(MOD_STAMP_TO_PACKAGES_MAP, data);
      }
    }

    return Pair.getSecond(data);
  }

  private static @Nullable Map<String, String> loadPackagesMap(@NotNull VirtualFile dotPackagesFile) {
    try {
      final List<String> lines;
      if (ApplicationManager.getApplication().isUnitTestMode()) {
        lines = StringUtil.split(new String(dotPackagesFile.contentsToByteArray(), StandardCharsets.UTF_8), "\n");
      }
      else {
        lines = FileUtil.loadLines(dotPackagesFile.getPath(), "UTF-8");
      }

      final Map<String, String> result = new HashMap<>();

      for (String line : lines) {
        if (line.trim().isEmpty() || line.startsWith("#")) continue;

        final int colonIndex = line.indexOf(':');
        if (colonIndex > 0 && colonIndex < line.length() - 1) {
          final String packageName = line.substring(0, colonIndex).trim();
          final String encodedUri = line.substring(colonIndex + 1).trim();
          // need to protect '+' chars because URLDecoder.decode replaces '+' with space
          final String encodedUriWithoutPluses = StringUtil.replace(encodedUri, "+", "%2B");
          final String uri = URLUtil.decode(encodedUriWithoutPluses);
          final String packageUri = getAbsolutePackageRootPath(dotPackagesFile.getParent(), uri);
          if (!packageName.isEmpty() && packageUri != null) {
            result.put(packageName, packageUri);
          }
        }
      }

      return result;
    }
    catch (IOException e) {
      return null;
    }
  }

  private static @Nullable String getAbsolutePackageRootPath(@NotNull VirtualFile baseDir, @NotNull String uri) {
    if (uri.startsWith("file:/")) {
      final String pathAfterSlashes = StringUtil.trimEnd(StringUtil.trimLeading(StringUtil.trimStart(uri, "file:/"), '/'), "/");
      if (SystemInfo.isWindows && !ApplicationManager.getApplication().isUnitTestMode()) {
        if (pathAfterSlashes.length() > 2 && OSAgnosticPathUtil.startsWithWindowsDrive(pathAfterSlashes)) {
          return pathAfterSlashes;
        }
      }
      else {
        return "/" + pathAfterSlashes;
      }
    }
    else {
      return FileUtil.toCanonicalPath(baseDir.getPath() + "/" + uri);
    }

    return null;
  }
}
