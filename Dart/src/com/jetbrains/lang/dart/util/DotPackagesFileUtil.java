// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
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

  private static final Key<Pair<Long, Map<String, String>>> MOD_STAMP_TO_PACKAGES_MAP = Key.create("MOD_STAMP_TO_PACKAGES_MAP");

  @Nullable
  public static Map<String, String> getPackagesMap(@NotNull final VirtualFile dotPackagesFile) {
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

  @Nullable
  private static Map<String, String> loadPackagesMap(@NotNull final VirtualFile dotPackagesFile) {
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

  @Nullable
  private static String getAbsolutePackageRootPath(@NotNull final VirtualFile baseDir, @NotNull final String uri) {
    if (uri.startsWith("file:/")) {
      final String pathAfterSlashes = StringUtil.trimEnd(StringUtil.trimLeading(StringUtil.trimStart(uri, "file:/"), '/'), "/");
      if (SystemInfo.isWindows && !ApplicationManager.getApplication().isUnitTestMode()) {
        if (pathAfterSlashes.length() > 2 && Character.isLetter(pathAfterSlashes.charAt(0)) && ':' == pathAfterSlashes.charAt(1)) {
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
