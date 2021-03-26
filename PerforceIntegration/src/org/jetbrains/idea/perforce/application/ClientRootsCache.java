package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
* to fix a case when user sets "c:\depot" in his client specification instead of "C:\depot" and
* we don't want to convert to canonical path/file - for each path reported by P4
*/
@Service
public final class ClientRootsCache {
  private static final Logger LOG = Logger.getInstance(ClientRootsCache.class);
  private final Map<String, String> myRawToCanonical = new HashMap<>();
  private final Map<String, String> myCanonicalToRaw = new HashMap<>();

  private final Object myLock = new Object();

  public static ClientRootsCache getClientRootsCache(Project project) {
    return project.getService(ClientRootsCache.class);
  }

  String putGet(final String rawClientRoot) {
    synchronized (myLock) {
      if (!myRawToCanonical.containsKey(rawClientRoot)) {
        String converted = correctCase(rawClientRoot);
        LOG.debug("canonicalize " + rawClientRoot + " to " + converted);
        myCanonicalToRaw.put(converted, rawClientRoot);
        myRawToCanonical.put(rawClientRoot, converted);
      }
      return myRawToCanonical.get(rawClientRoot);
    }
  }

  private static String correctCase(String rawClientRoot) {
    String converted = rawClientRoot;
    if (SystemInfo.isWindows && // assume the path has already correct case on other systems
        !"null".equals(rawClientRoot) &&
        !isUnixPath(rawClientRoot)) {
      final File root = new File(rawClientRoot);
      try {
        converted = root.getCanonicalPath();
      }
      catch (IOException ignore) {
      }
      if (rawClientRoot.endsWith(":") && rawClientRoot.length() == 2) { //disk drive letter
        converted = StringUtil.trimEnd(converted, File.separator);
      }
    }
    return converted;
  }

  private static boolean isUnixPath(String root) {
    return root.startsWith("/");
  }

  String getRaw(@Nullable final String converted) {
    synchronized (myLock) {
      final String was = myCanonicalToRaw.get(converted);
      return was == null ? converted : was;
    }
  }

  // faster than File canonical path
  String convertPath(@Nullable final String convertedClientRoot, @NotNull final String s) {
    final String trimmed = s.trim();
    synchronized (myLock) {
      if (convertedClientRoot != null) {
        final String rawRoot = myCanonicalToRaw.get(convertedClientRoot);
        if (rawRoot != null && FileUtil.startsWith(trimmed, rawRoot)) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("convertPath " + trimmed + " with rawRoot " + rawRoot + ", convertedClientRoot=" + convertedClientRoot);
          }
          return glueRelativePath(convertedClientRoot, trimmed.substring(rawRoot.length()));
        }
        // no info anyway
        return trimmed;
      }
      // check all
      for (String raw : myRawToCanonical.keySet()) {
        if (FileUtil.startsWith(trimmed, raw)) {
          final String canonical = myRawToCanonical.get(raw);
          if (LOG.isDebugEnabled()) {
            LOG.debug("convertPath " + trimmed + " with raw=" + raw + ", canonical=" + canonical);
          }
          return glueRelativePath(canonical, trimmed.substring(raw.length()));
        }
      }
      return trimmed;
    }
  }

  private static String glueRelativePath(final String absPath, final String relativePath) {
    if (absPath.endsWith("\\") || absPath.endsWith("/") || relativePath.startsWith("\\") || relativePath.startsWith("/")) {
      return absPath + relativePath;
    }
    return absPath + File.separator + relativePath;
  }
}
