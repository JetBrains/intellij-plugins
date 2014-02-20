package com.intellij.javascript.karma.execution;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.stream.JsonReader;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.text.SemVer;
import com.intellij.webcore.util.JsonUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class NodePackageVersionUtil {

  private static final Logger LOG = Logger.getInstance(NodePackageVersionUtil.class);

  private static final Map<String, TimestampedSemVer> PACKAGE_VERSION_CACHE =
    Collections.synchronizedMap(
      new LinkedHashMap<String, TimestampedSemVer>() {
        protected boolean removeEldestEntry(Map.Entry<String, TimestampedSemVer> eldest) {
          return size() > 10;
        }
      }
    );

  @Nullable
  public static SemVer getPackageVersion(@NotNull File packageDir) {
    File packageJson = new File(packageDir, "package.json");
    if (!packageJson.isFile()) {
      return null;
    }
    long lastModified = packageJson.lastModified();
    String path = packageDir.getAbsolutePath();
    TimestampedSemVer r = PACKAGE_VERSION_CACHE.get(path);
    if (r == null || r.getTimestamp() != lastModified) {
      SemVer semver = calc(packageJson);
      r = new TimestampedSemVer(lastModified, semver);
      PACKAGE_VERSION_CACHE.put(path, r);
    }
    return r.getSemVer();
  }

  @Nullable
  private static SemVer calc(@NotNull File packageJson) {
    JsonReader jsonReader = null;
    try {
      BufferedReader reader = Files.newReader(packageJson, Charsets.UTF_8);
      jsonReader = new JsonReader(reader);
      String version = JsonUtil.getChildAsString(jsonReader, "version");
      if (version != null) {
        return SemVer.parseFromText(version);
      }
    }
    catch (IOException e) {
      LOG.warn(e);
    }
    finally {
      if (jsonReader != null) {
        try {
          jsonReader.close();
        }
        catch (IOException ignored) {
        }
      }
    }
    return null;
  }

  private static class TimestampedSemVer {
    private final long myTimestamp;
    private final SemVer mySemVer;

    private TimestampedSemVer(long timestamp, @Nullable SemVer semVer) {
      myTimestamp = timestamp;
      mySemVer = semVer;
    }

    public long getTimestamp() {
      return myTimestamp;
    }

    @Nullable
    public SemVer getSemVer() {
      return mySemVer;
    }
  }
}
