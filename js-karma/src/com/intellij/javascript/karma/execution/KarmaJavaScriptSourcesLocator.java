package com.intellij.javascript.karma.execution;

import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * @author Sergey Simonchik
 */
public class KarmaJavaScriptSourcesLocator {

  private static final String SERVER_APP_FILE = "js_reporter/karma-server.js";
  private static final String CLIENT_APP_FILE = "js_reporter/karma-client.js";

  private KarmaJavaScriptSourcesLocator() {}

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
    File file = new File(sourceDir, baseName);
    if (!file.isFile()) {
      throw new IOException("Can't find " + file.getAbsolutePath());
    }
    return file;
  }

  @NotNull
  private static File getSourceDir() throws IOException {
    String jarPath = PathUtil.getJarPathForClass(KarmaJavaScriptSourcesLocator.class);
    if (jarPath.endsWith(".jar")) {
      File jarFile = new File(jarPath);
      if (!jarFile.isFile()) {
        throw new RuntimeException("jar file cannot be null");
      }
      File pluginBaseDir = jarFile.getParentFile().getParentFile();
      return new File(pluginBaseDir, "js-reporter");
    }
    return new File(jarPath);
  }

}
