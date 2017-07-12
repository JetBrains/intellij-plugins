package com.intellij.javascript.karma.server;

import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * @author Sergey Simonchik
 */
public class KarmaJsSourcesLocator {
  private static final String KARMA_INTELLIJ_NAME = "karma-intellij";
  private static final String JS_REPORTER_NAME = "js_reporter";

  private final File myKarmaIntellijPackageDir;

  public KarmaJsSourcesLocator(@NotNull NodePackage karmaPackage) throws IOException {
    myKarmaIntellijPackageDir = findKarmaIntellijPackageDir(karmaPackage);
  }

  /**
   * 'karma-intellij' directory contains installed node module 'karma-intellij'
   * Source code: https://github.com/karma-runner/karma-intellij
   *
   * @param karmaPackage 'karma' package
   * @return 'karma-intellij' directory
   */
  @NotNull
  private static File findKarmaIntellijPackageDir(@NotNull NodePackage karmaPackage) {
    if (true) {
      return new File("/home/segrey/work/idea-master/contrib/js-karma/src/js_reporter/karma-intellij");
    }
    File parentDir = new File(karmaPackage.getSystemDependentPath()).getParentFile();
    if (parentDir != null && parentDir.isAbsolute() && parentDir.isDirectory()) {
      File dir = new File(parentDir, KARMA_INTELLIJ_NAME);
      if (dir.isDirectory()) {
        return dir;
      }
    }
    File jsReporterDir = getBundledJsReporterDir();
    if (!jsReporterDir.isDirectory()) {
      throw new RuntimeException("Can't find bundled version of karma-intellij node module!");
    }
    return new File(jsReporterDir, KARMA_INTELLIJ_NAME);
  }

  private static File getBundledJsReporterDir() {
    String jarPath = PathUtil.getJarPathForClass(KarmaServer.class);
    if (!jarPath.endsWith(".jar")) {
      return new File(jarPath, JS_REPORTER_NAME);
    }
    File jarFile = new File(jarPath);
    if (!jarFile.isFile()) {
      throw new RuntimeException("jar file cannot be null");
    }
    File pluginBaseDir = jarFile.getParentFile().getParentFile();
    return new File(pluginBaseDir, JS_REPORTER_NAME);
  }

  private File getAppFile(@NotNull String baseName) throws IOException {
    File file = new File(myKarmaIntellijPackageDir, "lib" + File.separatorChar + baseName);
    if (!file.isFile()) {
      throw new IOException("Can't find " + file);
    }
    return file;
  }

  @NotNull
  public File getServerAppFile() throws IOException {
    return getAppFile("intellijServer.js");
  }

  @NotNull
  public File getClientAppFile() throws IOException {
    return getAppFile("intellijRunner.js");
  }
}
