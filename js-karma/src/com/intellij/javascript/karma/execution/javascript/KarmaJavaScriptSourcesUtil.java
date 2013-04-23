package com.intellij.javascript.karma.execution.javascript;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.regex.Pattern;

/**
 * @author Sergey Simonchik
 */
public class KarmaJavaScriptSourcesUtil {

  private static final String PLUGIN_NAME = "Karma";
  private static final String SERVER_APP_FILE = "karma-runner.js";
  private static final String CLIENT_APP_FILE = "karma-runner.js";
  private static final String[] NEEDED_RESOURCES = new String[] {
    "config.js",
    "intellij-reporter.js",
    "tree.js",
    SERVER_APP_FILE,
    CLIENT_APP_FILE
  };

  private KarmaJavaScriptSourcesUtil() {}

  @NotNull
  public static File getServerAppFile() throws IOException {
    return getFile(SERVER_APP_FILE);
  }

  @NotNull
  public static File getClientAppFile() throws IOException {
    return getFile(CLIENT_APP_FILE);
  }

  @NotNull
  private static File getFile(@NotNull String baseName) throws IOException {
    File sourceDir = getSourceDir();
    File serverAppFile = new File(sourceDir, baseName);
    if (!serverAppFile.isFile()) {
      FileUtil.delete(sourceDir);
      sourceDir = getSourceDir();
      serverAppFile = new File(sourceDir, baseName);
      if (!serverAppFile.isFile()) {
        throw new IOException("Can't find " + baseName);
      }
    }
    return serverAppFile;
  }

  @NotNull
  private static File getKarmaDir() {
    File javascriptDir = new File(PathManager.getSystemPath(), "javascript");
    File karmaDir = new File(javascriptDir, "karma");
    try {
      return karmaDir.getCanonicalFile();
    } catch (IOException e) {
      return karmaDir;
    }
  }

  @NotNull
  private static File getSourceDir() throws IOException {
    int[] versions = parseVersion();
    final File sourceDir;
    if (versions == null || versions.length != 2 || (versions[0] == 999 && versions[1] == 999)) {
      sourceDir = FileUtil.createTempDirectory("intellij-karma-", null);
    }
    else {
      File karmaDir = getKarmaDir();
      sourceDir = new File(karmaDir, versions[0] + "." + versions[1]);
    }
    if (!sourceDir.isDirectory()) {
      copyNeededFilesToDir(sourceDir);
    }
    return sourceDir;
  }

  private static void copyNeededFilesToDir(@NotNull File toDir) throws IOException {
    //noinspection ResultOfMethodCallIgnored
    toDir.mkdirs();
    if (!toDir.isDirectory()) {
      throw new IOException("Can't create directory " + toDir);
    }
    for (String baseName : NEEDED_RESOURCES) {
      File toFile = new File(toDir, baseName);
      InputStream inputStream = KarmaJavaScriptSourcesUtil.class.getResourceAsStream(baseName);
      try {
        if (inputStream == null) {
          throw new RuntimeException("Resource " + baseName + " is not found!");
        }
        copy(inputStream, toFile);
      }
      finally {
        if (inputStream != null) {
          inputStream.close();
        }
      }
    }
  }

  private static void copy(@NotNull InputStream inputStream, @NotNull File toFile) throws IOException {
    OutputStream outputStream = new FileOutputStream(toFile);
    try {
      FileUtil.copy(inputStream, outputStream);
    }
    finally {
      outputStream.close();
    }
  }

  @Nullable
  private static int[] parseVersion() {
    PluginId pluginId = PluginId.getId(PLUGIN_NAME);
    IdeaPluginDescriptor descriptor = PluginManager.getPlugin(pluginId);
    if (descriptor == null) {
      return null;
    }
    String vendor = descriptor.getVendor();
    if (vendor == null || !vendor.contains("JetBrains")) {
      return null;
    }
    String version = StringUtil.notNullize(descriptor.getVersion());
    String[] tokens = version.split(Pattern.quote("."));
    int[] versions = new int[tokens.length];
    for (int i = 0; i < tokens.length; i++) {
      try {
        int x = Integer.parseInt(tokens[i]);
        versions[i] = x;
      }
      catch (NumberFormatException e) {
        return null;
      }
    }
    return versions;
  }


}
