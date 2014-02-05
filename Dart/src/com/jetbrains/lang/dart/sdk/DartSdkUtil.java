package com.jetbrains.lang.dart.sdk;

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DartSdkUtil {

  private static Map<Pair<File, Long>, String> ourVersions = new HashMap<Pair<File, Long>, String>();

  @Nullable
  static String getSdkVersion(final @NotNull String sdkHomePath) {
    final File versionFile = new File(sdkHomePath + "/version");
    final File revisionFile = new File(sdkHomePath + "/revision");

    if (versionFile.isFile()) {
      final String cachedVersion = ourVersions.get(Pair.create(versionFile, versionFile.lastModified()));
      if (cachedVersion != null) return cachedVersion;
    }

    if (versionFile.isFile() && versionFile.length() < 100) {
      final String version;
      try {
        version = FileUtil.loadFile(versionFile).trim();
      }
      catch (IOException e) {
        return null;
      }

      String revision = null;
      if (revisionFile.isFile() && revisionFile.length() < 100) {
        try {
          revision = FileUtil.loadFile(revisionFile).trim();
        }
        catch (IOException ignore) {/* unlucky */}
      }

      final String versionWithRevision = revision == null || version.endsWith(revision) ? version : version + "_r" + revision;
      ourVersions.put(Pair.create(versionFile, versionFile.lastModified()), versionWithRevision);

      return versionWithRevision;
    }

    return null;
  }

  @Contract("null->false")
  public static boolean isDartSdkHome(final String path) {
    return path != null && !path.isEmpty() && new File(path + "/lib/core/core.dart").isFile();
  }
}
